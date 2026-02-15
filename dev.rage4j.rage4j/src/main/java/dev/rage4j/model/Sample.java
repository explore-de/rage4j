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

	private Sample(SampleBuilder builder)
	{
		this.question = builder.question;
		this.answer = builder.answer;
		this.groundTruth = builder.groundTruth;
		this.context = builder.context;
		this.images = builder.images == null ? null : List.copyOf(builder.images);
		this.comparisonSample = builder.comparisonSample;
	}

	public String getQuestion()
	{
		return question;
	}

	public String getQuestionOrFail()
	{
		if (Objects.isNull(question))
		{
			throwAttributeNotFound("question");
		}
		return question;
	}

	public boolean hasQuestion()
	{
		return question != null;
	}

	public String getAnswer()
	{
		return answer;
	}

	public String getAnswerOrFail()
	{
		if (Objects.isNull(answer))
		{
			throwAttributeNotFound("answer");
		}
		return answer;
	}

	public boolean hasAnswer()
	{
		return answer != null;
	}

	public String getGroundTruth()
	{
		return groundTruth;
	}

	public String getGroundTruthOrFail()
	{
		if (Objects.isNull(groundTruth))
		{
			throwAttributeNotFound("groundTruth");
		}
		return groundTruth;
	}

	public boolean hasGroundTruth()
	{
		return groundTruth != null;
	}

	public String getContext()
	{
		return context;
	}

	public String getContextOrFail()
	{
		if (Objects.isNull(context))
		{
			throwAttributeNotFound("context");
		}
		return context;
	}

	public boolean hasContext()
	{
		return context != null;
	}

	public List<String> getContextsListOrFail()
	{
		return List.of(getContextOrFail());
	}

	public boolean hasContextsList()
	{
		return hasContext();
	}

	public List<Rage4jImage> getImages()
	{
		return images == null ? Collections.emptyList() : images;
	}

	public boolean hasImages()
	{
		return images != null && !images.isEmpty();
	}

	public Sample getComparisonSample()
	{
		return comparisonSample;
	}

	public Sample getComparisonSampleOrFail()
	{
		if (Objects.isNull(comparisonSample))
		{
			throwAttributeNotFound("comparisonSample");
		}
		return comparisonSample;
	}

	public boolean hasComparisonSample()
	{
		return comparisonSample != null;
	}

	public Sample getControlSample()
	{
		return comparisonSample;
	}

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

	private void throwAttributeNotFound(String attribute)
	{
		throw new IllegalStateException("Attribute not found: " + attribute);
	}

	public static SampleBuilder builder()
	{
		return new SampleBuilder();
	}

	public static class SampleBuilder
	{
		private String question;
		private String answer;
		private String groundTruth;
		private String context;
		private List<Rage4jImage> images;
		private Sample comparisonSample;

		public SampleBuilder withQuestion(String question)
		{
			this.question = question;
			return this;
		}

		public SampleBuilder withAnswer(String answer)
		{
			this.answer = answer;
			return this;
		}

		public SampleBuilder withGroundTruth(String groundTruth)
		{
			this.groundTruth = groundTruth;
			return this;
		}

		public SampleBuilder withContext(String context)
		{
			this.context = context;
			return this;
		}

		public SampleBuilder withContextsList(List<String> contextsList)
		{
			this.context = contextsList == null ? null : String.join("\n", contextsList);
			return this;
		}

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

		public SampleBuilder withImages(List<Rage4jImage> images)
		{
			this.images = images == null ? null : new ArrayList<>(images);
			return this;
		}

		public SampleBuilder withComparisonSample(Sample comparisonSample)
		{
			this.comparisonSample = comparisonSample;
			return this;
		}

		public SampleBuilder withControlSample(Sample controlSample)
		{
			return withComparisonSample(controlSample);
		}

		public Sample build()
		{
			return new Sample(this);
		}
	}
}
