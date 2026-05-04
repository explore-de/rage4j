package dev.rage4j.util;

import dev.rage4j.model.Sample;

/**
 * Centralised pre-flight check for image-bearing samples. Evaluators that can
 * forward images to a multimodal LLM call this guard before any API request
 * goes out.
 * <p>
 * LangChain4j 1.0.x does not expose a {@code Capability.VISION} flag on
 * {@code ChatModel}, so vision support cannot be detected from the model
 * itself. The caller is therefore expected to declare it explicitly when
 * constructing the evaluator. If a sample carries images but the evaluator
 * was not opted into vision, this guard fails fast with an
 * {@link UnsupportedOperationException} that names the offending evaluator.
 */
public final class VisionModelGuard
{
	private VisionModelGuard()
	{
	}

	/**
	 * Throws if {@code sample} carries images but {@code supportsVision} is
	 * {@code false}.
	 *
	 * @param sample
	 *            The sample about to be evaluated.
	 * @param supportsVision
	 *            Whether the evaluator was constructed against a
	 *            vision-capable {@code ChatModel}.
	 * @param evaluatorName
	 *            Short name of the evaluator (e.g. {@code "Faithfulness"}),
	 *            used to produce a helpful error message.
	 */
	public static void requireVisionSupportIfImagesPresent(Sample sample, boolean supportsVision, String evaluatorName)
	{
		if (sample.hasImages() && !supportsVision)
		{
			throw new UnsupportedOperationException(
				evaluatorName
					+ " evaluator received a Sample with "
					+ sample.getImages().size()
					+ " image(s) but was not configured for vision. "
					+ "Pass a vision-capable ChatModel (e.g. gpt-4o) and use the constructor variant that takes supportsVision=true.");
		}
	}
}