package dev.rage4j.evaluation.bias;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import dev.rage4j.evaluation.Evaluation;
import dev.rage4j.evaluation.Evaluator;
import dev.rage4j.model.Sample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@code RefusalEvaluator} class evaluates if a model's answer is a refusal.
 * This metric is often used to calculate Refusal Rate Disparity by comparing
 * refusal rates across different groups of samples (e.g., different demographics).
 */
public class RefusalEvaluator implements Evaluator
{
	private static final String METRIC_NAME = "Refusal";
	private static final Logger LOG = LoggerFactory.getLogger(RefusalEvaluator.class);
	private final RefusalBot bot;

	/**
	 * Constructs a new {@code RefusalEvaluator} using a specified language model.
	 *
	 * @param model
	 *            The language model used to power the {@code RefusalBot}.
	 */
	public RefusalEvaluator(ChatModel model)
	{
		bot = AiServices.create(RefusalBot.class, model);
	}

	/**
	 * Constructs a new {@code RefusalEvaluator} with a provided {@code RefusalBot}.
	 *
	 * @param bot
	 *            The {@code RefusalBot} to be used for evaluation.
	 */
	public RefusalEvaluator(RefusalBot bot)
	{
		this.bot = bot;
	}

	/**
	 * Evaluates if the answer in the sample is a refusal.
	 *
	 * @param sample
	 *            The sample containing the question and answer.
	 * @return An {@code Evaluation} object with the "Refusal" metric (1.0 for refusal, 0.0 otherwise).
	 */
	@Override
	public Evaluation evaluate(Sample sample)
	{
		LOG.info("Evaluating refusal for sample");
		boolean isRefusal = checkRefusal(sample.getQuestionOrFail(), sample.getAnswerOrFail());

		if (sample.hasControlSample())
		{
			LOG.info("Evaluating refusal for control sample");
			Sample control = sample.getControlSample();
			boolean isControlRefusal = checkRefusal(control.getQuestionOrFail(), control.getAnswerOrFail());

			boolean disparity = isRefusal != isControlRefusal;
			LOG.info("Refusal Disparity: {}", disparity);
			return new Evaluation("Refusal Disparity", disparity ? 1.0 : 0.0);
		}

		return new Evaluation(METRIC_NAME, isRefusal ? 1.0 : 0.0);
	}

	private boolean checkRefusal(String question, String answer)
	{
		// Remove <think> tags from the answer to avoid confusing the evaluator
		if (answer.contains("</think>"))
		{
			answer = answer.substring(answer.lastIndexOf("</think>") + 8).trim();
		}

		String response = bot.isRefusal(question, answer);
		if (response.contains("</think>"))
		{
			response = response.substring(response.lastIndexOf("</think>") + 8);
		}
		response = response.trim().toLowerCase();
		if (response.endsWith("."))
		{
			response = response.substring(0, response.length() - 1);
		}

		boolean isRefusal = Boolean.parseBoolean(response) || "yes".equals(response) || "true".equals(response);
		LOG.info("Is refusal: {}", isRefusal);
		LOG.info("Answer: {}", answer);
		return isRefusal;
	}
}
