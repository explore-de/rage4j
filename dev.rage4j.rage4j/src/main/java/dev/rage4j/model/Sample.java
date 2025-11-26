package dev.rage4j.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * The {@code Sample} class represents a sample of data consisting of a
 * question, an answer, ground truth, and context. This class is used as an input
 * for evaluation, encapsulating the necessary data for evaluating a language
 * model's response against the ground truth and context.
 * <p>
 * Instances of {@code Sample} are created using the {@code SampleBuilder} class
 * to ensure all necessary fields are properly set before use.
 */
public class Sample implements Serializable
{
	@Serial
	private static final long serialVersionUID = 1L;

	protected String question;
	protected String answer;
	protected String groundTruth;
	protected String context;
	protected List<Rage4jImage> images;
	protected Sample comparisonSample;

	/**
	 * Private constructor to initialize a {@code Sample} object using a
	 * {@code SampleBuilder}.
	 *
	 * @param builder
	 *            The {@code SampleBuilder} containing the necessary fields for
	 *            creating a {@code Sample}.
	 */
	private Sample(SampleBuilder builder)
	{
		this.question = builder.question;
		this.answer = builder.answer;
		this.groundTruth = builder.groundTruth;
		this.context = builder.context;
		this.images = builder.images == null ? null : List.copyOf(builder.images);
		this.comparisonSample = builder.comparisonSample;
	}

	/**
	 * Returns the question of the sample.
	 *
	 * @return The question of the sample, or null if not set.
	 */
	public String getQuestion()
	{
		return question;
	}

	/**
	 * Returns the question of the sample. If the question is not set, throws an
	 * exception.
	 *
	 * @return The question of the sample.
	 * @throws IllegalStateException
	 *             if the question is not set.
	 */
	public String getQuestionOrFail()
	{
		if (Objects.isNull(question))
		{
			throwAttributeNotFound("question");
		}
		return question;
	}

	/**
	 * @return whether the sample has a question field.
	 */
	public boolean hasQuestion()
	{
		return question != null;
	}

	/**
	 * Returns the answer of the sample.
	 *
	 * @return The answer of the sample, or null if not set.
	 */
	public String getAnswer()
	{
		return answer;
	}

	/**
	 * Returns the answer of the sample. If the answer is not set, throws an
	 * exception.
	 *
	 * @return The answer of the sample.
	 * @throws IllegalStateException
	 *             if the answer is not set.
	 */
	public String getAnswerOrFail()
	{
		if (Objects.isNull(answer))
		{
			throwAttributeNotFound("answer");
		}
		return answer;
	}

	/**
	 * @return whether the sample has an answer field.
	 */
	public boolean hasAnswer()
	{
		return answer != null;
	}

	/**
	 * Returns the ground truth of the sample.
	 *
	 * @return The ground truth of the sample, or null if not set.
	 */
	public String getGroundTruth()
	{
		return groundTruth;
	}

	/**
	 * Returns the ground truth of the sample. If the ground truth is not set,
	 * throws an exception.
	 *
	 * @return The ground truth of the sample.
	 * @throws IllegalStateException
	 *             if the ground truth is not set.
	 */
	public String getGroundTruthOrFail()
	{
		if (Objects.isNull(groundTruth))
		{
			throwAttributeNotFound("groundTruth");
		}
		return groundTruth;
	}

	/**
	 * @return whether the groundTruth has a question field.
	 */
	public boolean hasGroundTruth()
	{
		return groundTruth != null;
	}

	/**
	 * Returns the context for the sample.
	 *
	 * @return The context for the sample, or null if not set.
	 */
	public String getContext()
	{
		return context;
	}

	/**
	 * Returns the context of the sample. If the context is not set, throws an
	 * exception.
	 *
	 * @return The context of the sample.
	 * @throws IllegalStateException
	 *             if the context is not set.
	 */
	public String getContextOrFail()
	{
		if (Objects.isNull(context))
		{
			throwAttributeNotFound("context");
		}
		return context;
	}

	/**
	 * @return whether the sample has a context field.
	 */
	public boolean hasContext()
	{
		return context != null;
	}

	/**
	 * Returns a single-item contexts list for compatibility with older callers.
	 *
	 * @return The context wrapped as a list.
	 * @throws IllegalStateException
	 *             if the context is not set.
	 */
	public List<String> getContextsListOrFail()
	{
		return List.of(getContextOrFail());
	}

	/**
	 * @return whether the sample has a context field.
	 */
	public boolean hasContextsList()
	{
		return hasContext();
	}

	/**
	 * Returns the images attached to the sample. The list is unmodifiable and
	 * never {@code null}.
	 *
	 * @return The (possibly empty) list of images.
	 */
	public List<Rage4jImage> getImages()
	{
		return images == null ? Collections.emptyList() : images;
	}

	/**
	 * @return whether the sample has at least one image attached.
	 */
	public boolean hasImages()
	{
		return images != null && !images.isEmpty();
	}

	/**
	 * Returns the comparison sample.
	 *
	 * @return The comparison sample, or null if not set.
	 */
	public Sample getComparisonSample()
	{
		return comparisonSample;
	}

	/**
	 * Returns the comparison sample. If the comparison sample is not set, throws
	 * an exception.
	 *
	 * @return The comparison sample.
	 * @throws IllegalStateException
	 *             if the comparison sample is not set.
	 */
	public Sample getComparisonSampleOrFail()
	{
		if (Objects.isNull(comparisonSample))
		{
			throwAttributeNotFound("comparisonSample");
		}
		return comparisonSample;
	}

	/**
	 * @return whether the sample has a comparison sample.
	 */
	public boolean hasComparisonSample()
	{
		return comparisonSample != null;
	}

	/**
	 * Returns the comparison sample using the legacy control-sample name.
	 *
	 * @return The comparison sample, or null if not set.
	 */
	public Sample getControlSample()
	{
		return comparisonSample;
	}

	/**
	 * @return whether the sample has a comparison sample.
	 */
	public boolean hasControlSample()
	{
		return hasComparisonSample();
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (o == null || getClass() != o.getClass())
		{
			return false;
		}
		Sample sample = (Sample)o;
		return Objects.equals(question, sample.question)
			&& Objects.equals(answer, sample.answer)
			&& Objects.equals(groundTruth, sample.groundTruth)
			&& Objects.equals(context, sample.context)
			&& Objects.equals(images, sample.images)
			&& Objects.equals(comparisonSample, sample.comparisonSample);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(question, answer, groundTruth, context, images, comparisonSample);
	}

	/**
	 * Helper method to throw an exception when a required attribute is not
	 * found.
	 *
	 * @param attribute
	 *            The name of the missing attribute.
	 * @throws IllegalStateException
	 *             Always thrown when this method is called.
	 */
	private void throwAttributeNotFound(String attribute)
	{
		throw new IllegalStateException("Attribute not found: " + attribute);
	}

	/**
	 * Creates and returns a new instance of {@code SampleBuilder} to build
	 * {@code Sample} objects.
	 *
	 * @return A new {@code SampleBuilder}.
	 */
	public static SampleBuilder builder()
	{
		return new SampleBuilder();
	}

	/**
	 * The {@code SampleBuilder} class provides a builder pattern to construct a
	 * {@code Sample} object. It allows for incremental construction of a sample
	 * by setting the question, answer, ground truth, context, images, and
	 * comparison sample.
	 */
	public static class SampleBuilder
	{
		private String question;
		private String answer;
		private String groundTruth;
		private String context;
		private List<Rage4jImage> images;
		private Sample comparisonSample;

		/**
		 * Sets the question for the {@code Sample}.
		 *
		 * @param question
		 *            The question to be evaluated.
		 * @return The current instance of {@code SampleBuilder}.
		 */
		public SampleBuilder withQuestion(String question)
		{
			this.question = question;
			return this;
		}

		/**
		 * Sets the answer for the {@code Sample}.
		 *
		 * @param answer
		 *            The answer to the question.
		 * @return The current instance of {@code SampleBuilder}.
		 */
		public SampleBuilder withAnswer(String answer)
		{
			this.answer = answer;
			return this;
		}

		/**
		 * Sets the ground truth for the {@code Sample}.
		 *
		 * @param groundTruth
		 *            The correct or expected answer to the question.
		 * @return The current instance of {@code SampleBuilder}.
		 */
		public SampleBuilder withGroundTruth(String groundTruth)
		{
			this.groundTruth = groundTruth;
			return this;
		}

		/**
		 * Sets the context for the {@code Sample}.
		 *
		 * @param context
		 *            The context string related to the question and answer.
		 * @return The current instance of {@code SampleBuilder}.
		 */
		public SampleBuilder withContext(String context)
		{
			this.context = context;
			return this;
		}

		/**
		 * Sets the context from a list of context strings for compatibility with
		 * older callers.
		 *
		 * @param contextsList
		 *            The list of context strings related to the question and
		 *            answer.
		 * @return The current instance of {@code SampleBuilder}.
		 */
		public SampleBuilder withContextsList(List<String> contextsList)
		{
			this.context = contextsList == null ? null : String.join("\n", contextsList);
			return this;
		}

		/**
		 * Adds a single image to the {@code Sample}. May be called multiple
		 * times to attach several images. Images are passed to vision-capable
		 * evaluators alongside the textual context.
		 *
		 * @param image
		 *            The image to add.
		 * @return The current instance of {@code SampleBuilder}.
		 */
		public SampleBuilder withImage(Rage4jImage image)
		{
			Objects.requireNonNull(image, "image");
			if (this.images == null)
			{
				this.images = new ArrayList<>();
			}
			this.images.add(image);
			return this;
		}

		/**
		 * Replaces the image list of the {@code Sample}. Pass {@code null} or
		 * an empty list to clear it.
		 *
		 * @param images
		 *            The list of images.
		 * @return The current instance of {@code SampleBuilder}.
		 */
		public SampleBuilder withImages(List<Rage4jImage> images)
		{
			this.images = images == null ? null : new ArrayList<>(images);
			return this;
		}

		/**
		 * Sets the comparison sample for the {@code Sample}.
		 *
		 * @param comparisonSample
		 *            The comparison sample to be compared against.
		 * @return The current instance of {@code SampleBuilder}.
		 */
		public SampleBuilder withComparisonSample(Sample comparisonSample)
		{
			this.comparisonSample = comparisonSample;
			return this;
		}

		/**
		 * Sets the comparison sample using the legacy control-sample name.
		 *
		 * @param controlSample
		 *            The comparison sample to be compared against.
		 * @return The current instance of {@code SampleBuilder}.
		 */
		public SampleBuilder withControlSample(Sample controlSample)
		{
			return withComparisonSample(controlSample);
		}

		/**
		 * Builds and returns a {@code Sample} object with the provided
		 * question, answer, ground truth, context, images, and comparison
		 * sample.
		 *
		 * @return A new {@code Sample} object.
		 */
		public Sample build()
		{
			return new Sample(this);
		}
	}
}
