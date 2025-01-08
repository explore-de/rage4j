package dev.rage4j.evaluation.faithfulness;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.AiServices;
import dev.rage4j.evaluation.Evaluation;
import dev.rage4j.evaluation.Evaluator;
import dev.rage4j.model.Sample;
import org.apache.commons.math3.analysis.function.Divide;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * The {@code FaithfulnessEvaluator} class provides a mechanism to evaluate the
 * faithfulness of a language model's answer by checking how well its claims can
 * be inferred from a given context. It uses a language model-based bot to
 * extract claims from the answer and then compares these claims to the provided
 * context.
 * <p>
 * The metric used is the fraction of claims that can be inferred from the
 * context, labeled as "Faithfulness."
 */
public class FaithfulnessEvaluator implements Evaluator
{
	private static final String METRIC_NAME = "Faithfulness";
	private static final Logger LOG = LoggerFactory.getLogger(FaithfulnessEvaluator.class);
	private final FaithfulnessBot bot;

	/**
	 * Constructs a new {@code FaithfulnessEvaluator} that evaluates
	 * faithfulness using a specified language model.
	 *
	 * @param model
	 *            The language model used to power the {@code FaithfulnessBot}
	 *            for evaluating answers.
	 */
	public FaithfulnessEvaluator(ChatLanguageModel model)
	{
		bot = AiServices.create(FaithfulnessBot.class, model);
	}

	/**
	 * Constructs a new {@code FaithfulnessEvaluator} with a provided
	 * {@code FaithfulnessBot}. This constructor is useful for testing purposes,
	 * where the {@code FaithfulnessBot} can be mocked and directly injected,
	 * bypassing the need to create it via {@code AiServices}.
	 *
	 * @param bot
	 *            The {@code FaithfulnessBot} to be used for evaluating the
	 *            faithfulness of a sample.
	 */
	public FaithfulnessEvaluator(FaithfulnessBot bot)
	{
		this.bot = bot;
	}

	/**
	 * Evaluates the faithfulness of a given answer by extracting claims from
	 * the answer and checking if they can be inferred from the provided
	 * context.
	 *
	 * @param sample
	 *            The sample containing the answer to evaluate and the context
	 *            for evaluation.
	 * @return An {@code Evaluation} object that contains the "Faithfulness"
	 *         metric and its computed score.
	 * @throws IllegalArgumentException
	 *             if the sample does not contain a valid answer or context.
	 */
	public Evaluation evaluate(Sample sample)
	{
		LOG.info("Evaluating new sample");
		String answer = sample.getAnswerOrFail();
		LOG.info("Answer: {}", answer);
		List<String> contextsList = sample.getContextsListOrFail();
		String context = String.join(System.lineSeparator(), contextsList);
		LOG.info("Context: {}", context);
		String[] answerClaims = bot.extractClaims(answer).getItems();
		LOG.info("Extracted claims: {}", (Object)answerClaims);

		double inferredClaimsCount = getInferredClaimsCount(context, answerClaims);
		LOG.info("Inferred claims count: {}", inferredClaimsCount);
		if (inferredClaimsCount == 0.0)
		{
			LOG.info("No true positives, false positives, or false negatives found.");
			return new Evaluation(METRIC_NAME, 0.0);
		}
		double inferredToAllFraction = new Divide().value(inferredClaimsCount, answerClaims.length);
		LOG.info("Faithfulness Metric: {}", inferredToAllFraction);
		return new Evaluation(METRIC_NAME, inferredToAllFraction);
	}

	/**
	 * Counts the number of claims from the answer that can be inferred from the
	 * given context.
	 *
	 * @param context
	 *            The context text to evaluate against.
	 * @param claims
	 *            The claims extracted from the answer.
	 * @return The number of claims that can be inferred from the context.
	 */
	private long getInferredClaimsCount(String context, String[] claims)
	{
		return Arrays.stream(claims)
			.filter(claim -> bot.canBeInferred(context, claim))
			.count();
	}
}
