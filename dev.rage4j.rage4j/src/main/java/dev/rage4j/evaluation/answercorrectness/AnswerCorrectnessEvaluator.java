package dev.rage4j.evaluation.answercorrectness;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import dev.rage4j.evaluation.Evaluation;
import dev.rage4j.evaluation.Evaluator;
import dev.rage4j.evaluation.model.ArrayResponse;
import dev.rage4j.model.Sample;
import org.apache.commons.math3.analysis.function.Divide;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@code AnswerCorrectnessEvaluator} class evaluates the correctness of an
 * answer by comparing it to the ground truth using true positive, false
 * positive, and false negative claims. The result is expressed as an F1 score,
 * which balances precision and recall.
 */
public class AnswerCorrectnessEvaluator implements Evaluator
{
	private static final String METRIC_NAME = "Answer correctness";
	private static final Logger LOG = LoggerFactory.getLogger(AnswerCorrectnessEvaluator.class);

	private final AnswerCorrectnessBot bot;

	/**
	 * Constructs an {@code AnswerCorrectnessEvaluator} using a
	 * {@code ChatModel}. The evaluator creates an instance of
	 * {@code AnswerCorrectnessBot} to assess the correctness of an answer.
	 *
	 * @param model
	 *            The {@code ChatModel} used to create the bot for evaluation.
	 */
	public AnswerCorrectnessEvaluator(ChatModel model)
	{
		bot = AiServices.create(AnswerCorrectnessBot.class, model);
	}

	/**
	 * Constructs an {@code AnswerCorrectnessEvaluator} with an existing
	 * {@code AnswerCorrectnessBot}. This constructor is useful for testing or
	 * custom scenarios where the bot is injected.
	 *
	 * @param answerCorrectnessBot
	 *            The bot used to evaluate the correctness of answers.
	 */
	public AnswerCorrectnessEvaluator(AnswerCorrectnessBot answerCorrectnessBot)
	{
		this.bot = answerCorrectnessBot;
	}

	/**
	 * Evaluates the correctness of the provided sample's answer against its
	 * ground truth. The correctness is calculated based on true positives,
	 * false positives, and false negatives, and the result is returned as an F1
	 * score.
	 *
	 * @param sample
	 *            The sample containing the answer and ground truth to be
	 *            evaluated.
	 * @return An {@code Evaluation} object containing the F1 score and the
	 *         metric name.
	 * @throws IllegalStateException
	 *             if either the answer or ground truth is missing in the
	 *             sample.
	 */
	@Override
	public Evaluation evaluate(Sample sample)
	{
		String groundTruth = sample.getGroundTruthOrFail();
		String answer = sample.getAnswerOrFail();
		LOG.info("Evaluating new sample");
		LOG.info("Ground truth: {}", groundTruth);
		LOG.info("Answer: {}", answer);

		ArrayResponse truePositiveClaims = bot.extractTruePositiveClaims(groundTruth, answer);
		double truePositives = truePositiveClaims.getLength();
		ArrayResponse falsePositiveClaims = bot.extractFalsePositiveClaims(groundTruth, answer);
		double falsePositives = falsePositiveClaims.getLength();
		ArrayResponse falseNegativeClaims = bot.extractFalseNegativeClaims(groundTruth, answer);
		double falseNegatives = falseNegativeClaims.getLength();

		if (truePositives == 0 && falsePositives == 0 && falseNegatives == 0)
		{
			LOG.info("No true positives, false positives, or false negatives found.");
			return new Evaluation(METRIC_NAME, 0);
		}
		else
		{
			LOG.info("True positives: {}", (Object)truePositiveClaims.getItems());
			LOG.info("False positives: {}", (Object)falsePositiveClaims.getItems());
			LOG.info("False negatives: {}", (Object)falseNegativeClaims.getItems());
		}

		double denominator = truePositives + new Divide().value(falsePositives + falseNegatives, 2);
		double f1Metric = new Divide().value(truePositives, denominator);

		LOG.info("Answer Correctness (F1) Metric: {}", f1Metric);

		return new Evaluation(METRIC_NAME, f1Metric);
	}
}