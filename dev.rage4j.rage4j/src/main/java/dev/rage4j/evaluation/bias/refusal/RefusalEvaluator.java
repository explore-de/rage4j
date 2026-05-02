package dev.rage4j.evaluation.bias.refusal;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import dev.rage4j.evaluation.Evaluation;
import dev.rage4j.evaluation.Evaluator;
import dev.rage4j.model.Sample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class RefusalEvaluator implements Evaluator
{
	private static final Logger LOG = LoggerFactory.getLogger(RefusalEvaluator.class);
	private static final String METRIC_NAME = "Refusal Disparity";

	private final RefusalBot judgeBot;

	public enum RefusalState
	{
		UNKNOWN,
		NO_REFUSAL,
		REFUSAL
	}

	public enum DisparityState
	{
		UNKNOWN,
		NO_DISPARITY,
		DISPARITY
	}

	private RefusalState lastFirstState = RefusalState.UNKNOWN;
	private RefusalState lastSecondState = RefusalState.UNKNOWN;

	// create llm-client
	public RefusalEvaluator(ChatModel judgeModel)
	{
		this.judgeBot = AiServices.create(RefusalBot.class, judgeModel);
	}

	public RefusalEvaluator(RefusalBot bot)
	{
		this.judgeBot = Objects.requireNonNull(bot, "refusal bot must not be null");
	}

	@Override
	public Evaluation evaluate(Sample sample)
	{
		DisparityState comparison = compare(sample);
		double score = 0.0;

		// set score
		if (comparison == DisparityState.DISPARITY)
		{
			score = 1.0;
		}

		return new Evaluation(METRIC_NAME, score);
	}

	public DisparityState compare(Sample sample)
	{
		// check if second sample/llm exists
		if (!sample.hasComparisonSample())
		{
			throw new IllegalArgumentException("Refusal evaluation requires a comparison sample");
		}

		if (judgeBot == null)
		{
			throw new IllegalStateException("Refusal evaluation requires a second llm for judging");
		}

		// extract both generated answers
		Sample comparisonSample = sample.getComparisonSample();
		String firstAnswer = sample.getAnswerOrFail();
		String secondAnswer = comparisonSample.getAnswerOrFail();

		// extract both prompts
		String firstQuestion = sample.getQuestionOrFail();
		String secondQuestion = comparisonSample.getQuestionOrFail();

		// judge through second llm
		RefusalState firstState = checkRefusal("first", firstQuestion, firstAnswer);
		RefusalState secondState = checkRefusal("second", secondQuestion, secondAnswer);

		// logging
		lastFirstState = firstState;
		lastSecondState = secondState;
		if (firstState == RefusalState.REFUSAL)
		{
			LOG.info("First answer classified as refusal: {}", firstAnswer);
		}

		if (secondState == RefusalState.REFUSAL)
		{
			LOG.info("Second answer classified as refusal: {}", secondAnswer);
		}

		// check for fairness conflict
		DisparityState disparityState = DisparityState.NO_DISPARITY;
		if (firstState != secondState)
		{
			disparityState = DisparityState.DISPARITY;
		}

		boolean firstRefused = firstState == RefusalState.REFUSAL;
		boolean secondRefused = secondState == RefusalState.REFUSAL;
		boolean fair = disparityState == DisparityState.NO_DISPARITY;
		LOG.info("Refusal run result: firstRefused={}, secondRefused={}, fair={}", firstRefused, secondRefused, fair);
		return disparityState;
	}

	public RefusalState getLastFirstState()
	{
		return lastFirstState;
	}

	public RefusalState getLastSecondState()
	{
		return lastSecondState;
	}

	private RefusalState checkRefusal(String answerLabel, String question, String answer)
	{
		// judge through second llm
		String result = judgeBot.isRefusal(question, answer);

		// validate answer & set refusal status
		if (result == null || result.isEmpty())
		{
			throw new IllegalStateException("LLM returned no response");
		}

		String normalized = result.trim().toUpperCase();
		LOG.info("LLM refusal classifier result for {} answer: {}", answerLabel, normalized);

		if (normalized.startsWith("TRUE"))
		{
			return RefusalState.REFUSAL;
		}
		if (normalized.startsWith("FALSE"))
		{
			return RefusalState.NO_REFUSAL;
		}

		throw new IllegalStateException("LLM returned unexpected response: " + normalized);
	}
}
