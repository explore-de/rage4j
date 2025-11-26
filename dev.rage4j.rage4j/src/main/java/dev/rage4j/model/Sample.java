package dev.rage4j.model;

import java.util.List;
import java.util.Objects;

/**
 * The {@code Sample} class represents a sample of data consisting of a
 * question, an answer, ground truth, and a list of contexts. This class is used
 * as an input for evaluation, encapsulating the necessary data for evaluating a
 * language model's response against the ground truth and context.
 * <p>
 * Instances of {@code Sample} are created using the {@code SampleBuilder} class
 * to ensure all necessary fields are properly set before use.
 */
public class Sample
{
	protected String question;
	protected String answer;
	protected String groundTruth;
	protected List<String> contextsList;

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
		this.contextsList = builder.contextsList;
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
	 * @return whether the groundTruth has a question field.
	 */
	public boolean hasGroundTruth()
	{
		return groundTruth != null;
	}

	/**
	 * Returns the list of contexts for the sample.
	 *
	 * @return The list of contexts for the sample, or null if not set.
	 */
	public List<String> getContextsList()
	{
		return contextsList;
	}

	/**
	 * @return whether the contextsList has a question field.
	 */
	public boolean hasContextsList()
	{
		return contextsList != null;
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
		Sample sample = (Sample) o;
		return Objects.equals(question, sample.question)
			&& Objects.equals(answer, sample.answer)
			&& Objects.equals(groundTruth, sample.groundTruth)
			&& Objects.equals(contextsList, sample.contextsList);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(question, answer, groundTruth, contextsList);
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
	 * by setting the question, answer, ground truth, and contexts list.
	 */
	public static class SampleBuilder
	{
		private String question;
		private String answer;
		private String groundTruth;
		private List<String> contextsList;

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
		 * Sets the list of contexts for the {@code Sample}.
		 *
		 * @param contextsList
		 *            The list of context strings related to the question and
		 *            answer.
		 * @return The current instance of {@code SampleBuilder}.
		 */
		public SampleBuilder withContextsList(List<String> contextsList)
		{
			this.contextsList = contextsList;
			return this;
		}

		/**
		 * Builds and returns a {@code Sample} object with the provided
		 * question, answer, ground truth, and contexts list.
		 *
		 * @return A new {@code Sample} object.
		 */
		public Sample build()
		{
			return new Sample(this);
		}
	}
}
