package dev.rage4j.evaluation.faithfulness;

import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import dev.rage4j.evaluation.Evaluation;
import dev.rage4j.evaluation.Evaluator;
import dev.rage4j.model.Rage4jImage;
import dev.rage4j.model.Sample;
import dev.rage4j.util.VisionModelGuard;
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
 * <p>
 * If the {@link Sample} carries images and the evaluator was constructed
 * against a vision-capable {@link ChatModel} (i.e. {@code supportsVision} is
 * {@code true}), the images are forwarded to the bot together with the textual
 * context. When images are present but the evaluator was not configured for
 * vision, an {@link UnsupportedOperationException} is thrown.
 */
public class FaithfulnessEvaluator implements Evaluator
{
	private static final String METRIC_NAME = "Faithfulness";
	private static final Logger LOG = LoggerFactory.getLogger(FaithfulnessEvaluator.class);
	private final FaithfulnessBot bot;
	private final boolean supportsVision;

	/**
	 * Constructs a new text-only {@code FaithfulnessEvaluator}. Samples that
	 * carry images will cause an {@link UnsupportedOperationException} at
	 * evaluation time. Use {@link #FaithfulnessEvaluator(ChatModel, boolean)}
	 * to opt into vision.
	 *
	 * @param model
	 *            The language model used to power the {@code FaithfulnessBot}
	 *            for evaluating answers.
	 */
	public FaithfulnessEvaluator(ChatModel model)
	{
		this(model, false);
	}

	/**
	 * Constructs a new {@code FaithfulnessEvaluator} that may optionally
	 * forward images from the {@link Sample} to the underlying bot.
	 *
	 * @param model
	 *            The language model used to power the {@code FaithfulnessBot}.
	 *            Must be vision-capable when {@code supportsVision} is
	 *            {@code true}.
	 * @param supportsVision
	 *            Whether the supplied {@code ChatModel} can handle multimodal
	 *            input. When {@code false}, samples carrying images cause an
	 *            {@link UnsupportedOperationException} at evaluation time.
	 */
	public FaithfulnessEvaluator(ChatModel model, boolean supportsVision)
	{
		this.bot = AiServices.create(FaithfulnessBot.class, model);
		this.supportsVision = supportsVision;
	}

	/**
	 * Constructs a new text-only {@code FaithfulnessEvaluator} with a provided
	 * {@code FaithfulnessBot}. This constructor is useful for testing purposes,
	 * where the bot can be mocked and directly injected, bypassing the need to
	 * create it via {@code AiServices}.
	 *
	 * @param bot
	 *            The {@code FaithfulnessBot} to be used for evaluating the
	 *            faithfulness of a sample.
	 */
	public FaithfulnessEvaluator(FaithfulnessBot bot)
	{
		this(bot, false);
	}

	/**
	 * Constructs a new {@code FaithfulnessEvaluator} with a provided
	 * {@code FaithfulnessBot} and an explicit vision-support flag. Useful for
	 * testing the multimodal code path with a mocked bot.
	 *
	 * @param bot
	 *            The {@code FaithfulnessBot} to be used for evaluating the
	 *            faithfulness of a sample.
	 * @param supportsVision
	 *            Whether the bot's underlying model is vision-capable.
	 */
	public FaithfulnessEvaluator(FaithfulnessBot bot, boolean supportsVision)
	{
		this.bot = bot;
		this.supportsVision = supportsVision;
	}

	/**
	 * Evaluates the faithfulness of a given answer by extracting claims from
	 * the answer and checking if they can be inferred from the provided context
	 * (and, if available and supported, images).
	 *
	 * @param sample
	 *            The sample containing the answer to evaluate and the context
	 *            for evaluation.
	 * @return An {@code Evaluation} object that contains the "Faithfulness"
	 *         metric and its computed score.
	 * @throws IllegalArgumentException
	 *             if the sample does not contain a valid answer or context.
	 * @throws UnsupportedOperationException
	 *             if the sample carries images but the evaluator was not
	 *             configured for vision.
	 */
	public Evaluation evaluate(Sample sample)
	{
		if (!sample.hasAnswer())
		{
			throw new IllegalArgumentException("Sample must have an answer for Faithfulness evaluation");
		}
		if (!sample.hasContext())
		{
			throw new IllegalArgumentException("Sample must have a context for Faithfulness evaluation");
		}
		VisionModelGuard.requireVisionSupportIfImagesPresent(sample, supportsVision, METRIC_NAME);

		LOG.info("Evaluating new sample");
		String answer = sample.getAnswer();
		LOG.info("Answer: {}", answer);
		String context = sample.getContext();
		LOG.info("Context: {}", context);
		List<ImageContent> images = mapImages(sample);
		if (!images.isEmpty())
		{
			LOG.info("Forwarding {} image(s) to faithfulness bot", images.size());
		}
		String[] answerClaims = bot.extractClaims(answer).getItems();
		LOG.info("Extracted claims: {}", (Object)answerClaims);

		double inferredClaimsCount = getInferredClaimsCount(context, answerClaims, images);
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

	private static List<ImageContent> mapImages(Sample sample)
	{
		if (!sample.hasImages())
		{
			return List.of();
		}
		return sample.getImages().stream()
			.map(Rage4jImage::toImageContent)
			.toList();
	}

	/**
	 * Counts the number of claims from the answer that can be inferred from the
	 * given context (and images, when present).
	 */
	private long getInferredClaimsCount(String context, String[] claims, List<ImageContent> images)
	{
		return Arrays.stream(claims)
			.filter(claim -> bot.canBeInferred(images, claim, context))
			.count();
	}
}