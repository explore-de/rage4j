package dev.rage4j.evaluation.contextrelevance.llm;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import dev.rage4j.evaluation.Evaluation;
import dev.rage4j.evaluation.Evaluator;
import dev.rage4j.model.Sample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContextRelevanceLlmEvaluator implements Evaluator
{

	private static final String METRIC_NAME = "Context relevance LLM";
	private static final double MIN_SCORE = 0.0;
	private static final double MAX_SCORE = 3.0;
	private static final Logger LOG = LoggerFactory.getLogger(ContextRelevanceLlmEvaluator.class);

	private final ContextRelevanceLlmBot bot;

	public ContextRelevanceLlmEvaluator(ChatModel judgeModel)
	{
		bot = AiServices.create(ContextRelevanceLlmBot.class, judgeModel);
	}

	public ContextRelevanceLlmEvaluator(ContextRelevanceLlmBot bot)
	{
		this.bot = bot;
	}

	/**
	 * Evaluates the given sample according to a specific metric and returns the
	 * result as an {@code Evaluation}.
	 *
	 * @param sample
	 *            The sample containing data (such as question and context) to be
	 *            evaluated.
	 * @return An {@code Evaluation} object representing the metric name and its
	 *         calculated value.
	 * @throws IllegalArgumentException
	 *             if the sample is invalid or cannot be evaluated.
	 */
	@Override
	public Evaluation evaluate(Sample sample)
	{
		if (!sample.hasContext())
		{
			throw new IllegalArgumentException("Sample must have a context for Context Relevance LLM evaluation");
		}
		if (!sample.hasQuestion())
		{
			throw new IllegalArgumentException("Sample must have a question for Context Relevance LLM evaluation");
		}

		String question = sample.getQuestion();
		String context = sample.getContext();
		LOG.info("Evaluating new sample");
		LOG.info("Question: {}", question);
		LOG.info("Context: {}", context);

		if (context.isBlank())
		{
			return new Evaluation(METRIC_NAME, 0.0);
		}

		String scoreRaw = bot.generateScore(question, context);

		int score = Integer.parseInt(scoreRaw);
		LOG.info("Raw score from LLM: {}", score);

		double result = normalize(score);
		LOG.info("Evaluation result: {}", result);

		return new Evaluation(METRIC_NAME, result);
	}

	private static double normalize(double score)
	{
		double normalized = (score - MIN_SCORE) / (MAX_SCORE - MIN_SCORE);
		return Math.clamp(normalized, 0.0, 1.0);
	}
}
