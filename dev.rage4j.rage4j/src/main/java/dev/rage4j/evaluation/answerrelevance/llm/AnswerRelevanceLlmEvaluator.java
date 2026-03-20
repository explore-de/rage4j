package dev.rage4j.evaluation.answerrelevance.llm;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import dev.rage4j.evaluation.Evaluation;
import dev.rage4j.evaluation.Evaluator;
import dev.rage4j.evaluation.model.ScoreWithReasonResponse;
import dev.rage4j.model.Sample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnswerRelevanceLlmEvaluator implements Evaluator
{

	private static final String METRIC_NAME = "Answer relevance llm";
	private static final Logger LOG = LoggerFactory.getLogger(AnswerRelevanceLlmEvaluator.class);

	private final AnswerRelevanceLlmBot bot;

	public AnswerRelevanceLlmEvaluator(ChatModel judgeModel)
	{
		bot = AiServices.create(AnswerRelevanceLlmBot.class, judgeModel);
	}

	/**
	 * Constructs a new {@code AnswerRelevanceLlmEvaluator} with a provided
	 * {@code AnswerRelevanceLlmBot}. This constructor is useful for testing
	 * purposes, where the {@code AnswerRelevanceLlmBot} can be mocked and
	 * directly injected, bypassing the need to create it via
	 * {@code AiServices}.
	 *
	 * @param bot
	 *            The {@code AnswerRelevanceLlmBot} to be used for generating
	 *            the relevance score.
	 */
	public AnswerRelevanceLlmEvaluator(AnswerRelevanceLlmBot bot)
	{
		this.bot = bot;
	}

	/**
	 * Evaluates the given sample according to a specific metric and returns the
	 * result as an {@code Evaluation}.
	 *
	 * @param sample
	 *            The sample containing data (such as context and answer) to be
	 *            evaluated.
	 * @return An {@code Evaluation} object representing the metric name and its
	 *         calculated value.
	 * @throws IllegalArgumentException
	 *             if the sample is invalid or cannot be evaluated.
	 */
	@Override
	public Evaluation evaluate(Sample sample)
	{
		if (!sample.hasAnswer())
		{
			throw new IllegalArgumentException("Sample must have an answer for Answer Relevance LLM evaluation");
		}
		if (!sample.hasQuestion())
		{
			throw new IllegalArgumentException("Sample must have a question for Answer Relevance LLM evaluation");
		}

		String answer = sample.getAnswer();
		String question = sample.getQuestion();
		LOG.info("Evaluating new sample");
		LOG.info("Question: {}", question);
		LOG.info("Answer: {}", answer);

		int score = parseScore(question, answer);

		return new Evaluation(METRIC_NAME, score);

	}

	private int parseScore(String question, String answer)
	{
		try
		{
			ScoreWithReasonResponse response = bot.generateScoreWithReason(question, answer);
			if (response != null && response.getScore() != null)
			{
				LOG.info("Judge reason: {}", response.getReason());
				return response.getScore();
			}
		}
		catch (RuntimeException e)
		{
			LOG.warn("Structured response parsing failed, falling back to numeric-only score", e);
		}

		String scoreGeneratedByJudge = bot.generateScore(question, answer);
		return Integer.parseInt(scoreGeneratedByJudge);
	}
}
