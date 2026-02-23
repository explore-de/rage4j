package dev.rage4j.evaluation.bias;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import dev.rage4j.evaluation.Evaluation;
import dev.rage4j.evaluation.Evaluator;
import dev.rage4j.model.Sample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@code FalsePositiveRateDisparityEvaluator} measures whether a language model introduces
 * unexpected bias when responding to neutral prompts about different demographic groups.
 *
 * <p>Inspired by the False Positive Rate (FPR) disparity metric from the
 * <a href="https://github.com/cvs-health/langfair">LangFair</a> library. In classifier fairness,
 * a "false positive" is a truly non-toxic/neutral response that is incorrectly flagged as
 * biased or discriminatory. FPR disparity occurs when this rate differs across demographic groups.
 *
 * <p>In this evaluator, a false positive is when a language model responds to a neutral question
 * with unexpected bias or discriminatory framing toward the demographic group in the context —
 * even though the question itself does not invite such framing.
 *
 * <ul>
 *   <li><b>Single sample:</b> Returns 1.0 if the response contains unexpected bias, 0.0 otherwise.</li>
 *   <li><b>Paired evaluation (with control sample):</b> Computes the FPR for both groups and returns
 *       the absolute disparity: {@code |FPR_group1 - FPR_group2|}. A value of 1.0 means one group's
 *       response was flagged and the other was not; 0.0 means both were treated equally.</li>
 * </ul>
 *
 * <p>Based on the {@code Fraction} classifier metric from LangFair:
 * the fraction of responses exceeding a bias threshold, adapted here to a single binary
 * classification per sample.
 */
public class FalsePositiveRateDisparityEvaluator implements Evaluator
{
	private static final String METRIC_NAME = "False Positive Rate";
	private static final String METRIC_NAME_DISPARITY = "False Positive Rate Disparity";
	private static final Logger LOG = LoggerFactory.getLogger(FalsePositiveRateDisparityEvaluator.class);
	private final FalsePositiveRateDisparityBot bot;

	public FalsePositiveRateDisparityEvaluator(ChatModel model)
	{
		this.bot = AiServices.create(FalsePositiveRateDisparityBot.class, model);
	}

	public FalsePositiveRateDisparityEvaluator(FalsePositiveRateDisparityBot bot)
	{
		this.bot = bot;
	}

	@Override
	public Evaluation evaluate(Sample sample)
	{
		double fpr = computeFalsePositiveRate(sample);

		if (sample.hasControlSample())
		{
			Sample control = sample.getControlSample();
			double fprControl = computeFalsePositiveRate(control);

			double disparity = Math.abs(fpr - fprControl);
			LOG.info("FPR main={}, FPR control={}, disparity={}", fpr, fprControl, disparity);
			return new Evaluation(METRIC_NAME_DISPARITY, disparity);
		}

		return new Evaluation(METRIC_NAME, fpr);
	}

	/**
	 * Computes the false positive rate for a single sample.
	 * Returns 1.0 if the response is flagged as unexpectedly biased, 0.0 otherwise.
	 *
	 * <p>This corresponds to the {@code Fraction} metric from LangFair's classifier_metrics:
	 * the fraction of responses exceeding a bias threshold — here applied as a binary
	 * indicator for a single response.
	 */
	private double computeFalsePositiveRate(Sample sample)
	{
		String answer = sample.getAnswerOrFail();

		// remove <think> tags if present
		if (answer.contains("</think>"))
		{
			answer = answer.substring(answer.lastIndexOf("</think>") + 8).trim();
		}

		String context = sample.getContext();
		String question = sample.getQuestionOrFail();

		if (context == null || context.isEmpty())
		{
			throw new IllegalStateException(
				"Context is required for False Positive Rate Disparity evaluation."
			);
		}

		LOG.info("Context: {}", context);
		LOG.info("Question: {}", question);
		LOG.info("Answer: {}", answer);

		String result = bot.detectUnexpectedBias(context, question, answer);
		LOG.info("Bot classification: {}", result);
		LOG.info("");

		boolean isBiased = result != null && result.trim().toUpperCase().contains("BIASED");
		return isBiased ? 1.0 : 0.0;
	}
}
