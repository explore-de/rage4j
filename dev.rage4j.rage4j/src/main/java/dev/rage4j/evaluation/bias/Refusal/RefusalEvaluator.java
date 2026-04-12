package dev.rage4j.evaluation.bias.Refusal;

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

	private final ChatModel evaluatedModel;
	private final ChatModel refusalDetectionModel;
	private final RefusalBot bot;

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

	public RefusalEvaluator(ChatModel evaluatedModel, ChatModel refusalDetectionModel)
	{
		this.evaluatedModel = evaluatedModel;
		this.refusalDetectionModel = Objects.requireNonNull(refusalDetectionModel, "refusal detection model must not be null");
		this.bot = AiServices.create(RefusalBot.class, this.refusalDetectionModel);
	}

	public RefusalEvaluator(RefusalBot bot)
	{
		this(null, bot);
	}

	public RefusalEvaluator(ChatModel evaluatedModel, RefusalBot bot)
	{
		this.evaluatedModel = evaluatedModel;
		this.refusalDetectionModel = null;
		this.bot = Objects.requireNonNull(bot, "refusal bot must not be null");
	}

	@Override
	public Evaluation evaluate(Sample sample)
	{
		DisparityState comparison = compare(sample);
		double score = 0.0;

		if (comparison == DisparityState.DISPARITY)
		{
			score = 1.0;
		}

		return new Evaluation(METRIC_NAME, score);
	}

	public DisparityState compare(Sample sample)
	{
		if (!sample.hasComparisonSample())
		{
			throw new IllegalArgumentException("Refusal evaluation requires a comparison sample");
		}

		Sample comparisonSample = sample.getComparisonSample();
		String firstAnswer = resolveAnswer(sample);
		String secondAnswer = resolveAnswer(comparisonSample);
		String firstQuestion = resolveQuestion(sample);
		String secondQuestion = resolveQuestion(comparisonSample);

		RefusalState firstState = checkRefusal("first", firstQuestion, firstAnswer);
		RefusalState secondState = checkRefusal("second", secondQuestion, secondAnswer);
		lastFirstState = firstState;
		lastSecondState = secondState;

		if (firstState == RefusalState.REFUSAL)
		{
			LOG.info("First answer was classified as refusal.");
		}
		if (secondState == RefusalState.REFUSAL)
		{
			LOG.info("Second answer was classified as refusal.");
		}

		DisparityState disparityState = DisparityState.NO_DISPARITY;
		if (firstState != secondState)
		{
			disparityState = DisparityState.DISPARITY;
		}

		boolean firstRefused = firstState == RefusalState.REFUSAL;
		boolean secondRefused = secondState == RefusalState.REFUSAL;
		boolean fair = disparityState == DisparityState.NO_DISPARITY;
		LOG.info("Refusal run result: firstRefused={}, secondRefused={}, fair={}",
			firstRefused, secondRefused, fair);
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

	public RefusalState checkRefusal(String answer)
	{
		return checkRefusal("", answer);
	}

	public RefusalState checkRefusal(String question, String answer)
	{
		return checkRefusal("unlabeled", question, answer);
	}

	private RefusalState checkRefusal(String answerLabel, String question, String answer)
	{
		String result = classifyRefusal(question, answer);
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

	private String classifyRefusal(String question, String answer)
	{
		return bot.isRefusal(question, answer);
	}

	private String resolveAnswer(Sample sample)
	{
		if (sample.hasAnswer())
		{
			return sample.getAnswerOrFail();
		}
		if (evaluatedModel == null)
		{
			throw new IllegalStateException(
				"Refusal evaluation requires an answer in the sample or an evaluated model in the constructor");
		}

		String question = sample.getQuestionOrFail();
		String answer = evaluatedModel.chat(question);
		if (answer == null)
		{
			throw new IllegalStateException("Evaluated model returned null for refusal evaluation");
		}
		return answer;
	}

	private String resolveQuestion(Sample sample)
	{
		if (!sample.hasQuestion())
		{
			return "";
		}
		return sample.getQuestionOrFail();
	}
}
