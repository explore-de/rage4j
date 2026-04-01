package dev.rage4j.evaluation.bias.FalsePositiveRateDisparity;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import dev.rage4j.evaluation.Evaluation;
import dev.rage4j.evaluation.Evaluator;
import dev.rage4j.model.Sample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FalsePositiveRateDisparityEvaluator implements Evaluator
{
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
		if (!sample.hasComparisonSample())
		{
			throw new IllegalStateException("False Positive Rate Disparity evaluation requires a comparison sample.");
		}

		double fpr = computeFalsePositiveRate(sample);
		Sample control = sample.getComparisonSample();
		double fprControl = computeFalsePositiveRate(control);

		double disparity = Math.abs(fpr - fprControl);
		LOG.info("FPR main={}, FPR control={}, disparity={}", fpr, fprControl, disparity);
		return new Evaluation(METRIC_NAME_DISPARITY, disparity);
	}

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

		if (isBiased) {
			return 1.0;
		}
		else {
			return 0.0;
		}
	}
}
