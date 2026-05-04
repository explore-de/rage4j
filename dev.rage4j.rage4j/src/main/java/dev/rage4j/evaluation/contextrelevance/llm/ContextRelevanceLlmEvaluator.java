package dev.rage4j.evaluation.contextrelevance.llm;

import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import dev.rage4j.evaluation.Evaluation;
import dev.rage4j.evaluation.Evaluator;
import dev.rage4j.model.Rage4jImage;
import dev.rage4j.model.Sample;
import dev.rage4j.util.VisionModelGuard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static dev.rage4j.util.ScoreParser.parseScore;

/**
 * Evaluates how well the context of a {@link Sample} addresses its question.
 * If the sample carries images and the evaluator was constructed against a
 * vision-capable {@link ChatModel} (i.e. {@code supportsVision} is
 * {@code true}), the images are forwarded to the bot together with the
 * textual context. When images are present but vision was not enabled, an
 * {@link UnsupportedOperationException} is thrown.
 */
public class ContextRelevanceLlmEvaluator implements Evaluator
{

	private static final String METRIC_NAME = "Context relevance LLM";
	private static final double MIN_SCORE = 0.0;
	private static final double MAX_SCORE = 3.0;
	private static final Logger LOG = LoggerFactory.getLogger(ContextRelevanceLlmEvaluator.class);

	private final ContextRelevanceLlmBot bot;
	private final boolean supportsVision;

	public ContextRelevanceLlmEvaluator(ChatModel judgeModel)
	{
		this(judgeModel, false);
	}

	public ContextRelevanceLlmEvaluator(ChatModel judgeModel, boolean supportsVision)
	{
		this.bot = AiServices.create(ContextRelevanceLlmBot.class, judgeModel);
		this.supportsVision = supportsVision;
	}

	public ContextRelevanceLlmEvaluator(ContextRelevanceLlmBot bot)
	{
		this(bot, false);
	}

	public ContextRelevanceLlmEvaluator(ContextRelevanceLlmBot bot, boolean supportsVision)
	{
		this.bot = bot;
		this.supportsVision = supportsVision;
	}

	/**
	 * Evaluates the given sample according to the context-relevance metric and
	 * returns the result as an {@code Evaluation}.
	 *
	 * @param sample
	 *            The sample containing question, context and optionally images.
	 * @return An {@code Evaluation} object with the metric name and value.
	 * @throws IllegalArgumentException
	 *             if the sample lacks a context or a question.
	 * @throws UnsupportedOperationException
	 *             if the sample carries images but the evaluator was not
	 *             configured for vision.
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
		VisionModelGuard.requireVisionSupportIfImagesPresent(sample, supportsVision, METRIC_NAME);

		String question = sample.getQuestion();
		String context = sample.getContext();
		List<ImageContent> images = mapImages(sample);
		LOG.info("Evaluating new sample");
		LOG.info("Question: {}", question);
		LOG.info("Context: {}", context);
		if (!images.isEmpty())
		{
			LOG.info("Forwarding {} image(s) to context-relevance bot", images.size());
		}

		// Without text context AND without images, there is nothing to score.
		if (context.isBlank() && images.isEmpty())
		{
			return new Evaluation(METRIC_NAME, 0.0);
		}

		String scoreRaw = bot.generateScore(images, question, context);

		int score = parseScore(scoreRaw);
		LOG.info("Raw score from LLM: {}", score);

		double result = normalize(score);
		LOG.info("Evaluation result: {}", result);

		return new Evaluation(METRIC_NAME, result);
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

	private static double normalize(double score)
	{
		double normalized = (score - MIN_SCORE) / (MAX_SCORE - MIN_SCORE);
		return Math.clamp(normalized, 0.0, 1.0);
	}
}