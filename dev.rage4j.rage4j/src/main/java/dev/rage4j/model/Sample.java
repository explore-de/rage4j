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
	private final String question;
	private final String answer;
	private final String groundTruth;
	private final List<String> contextsList;

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
	 * Returns the list of contexts for the sample. If the contexts list is not
	 * set, throws an exception.
	 *
	 * @return The list of contexts for the sample.
	 * @throws IllegalStateException
	 *             if the contexts list is not set.
	 */
	public List<String> getContextsListOrFail()
	{
		if (Objects.isNull(contextsList))
		{
			throwAttributeNotFound("contextsList");
		}
		return contextsList;
	}

	/**
	 * @return whether the contextsList has a question field.
	 */
	public boolean hasContextsList()
	{
		return contextsList != null;
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
