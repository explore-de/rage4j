package dev.rage4j.model;

import dev.rage4j.evaluation.Evaluation;

import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;

/**
 * The {@code EvaluationAggregation} class represents a map-like structure that
 * stores a collection of evaluation results, with metric names as keys and
 * their corresponding evaluation values as {@code Double}. This class extends
 * {@code HashMap} and provides mutable map operations.
 * <p>
 * Optionally, an instance can be associated with a {@code Sample} that was
 * evaluated.
 */
public class EvaluationAggregation extends HashMap<String, Double>
{
	private static final long serialVersionUID = 1L;
	private Sample sample;

	/**
	 * Constructs a new empty {@code EvaluationAggregation}.
	 */
	public EvaluationAggregation()
	{
		super();
	}

	/**
	 * Constructs a new {@code EvaluationAggregation} associated with the
	 * specified sample.
	 *
	 * @param sample
	 *            The sample that was evaluated.
	 */
	public EvaluationAggregation(Sample sample)
	{
		super();
		this.sample = sample;
	}

	/**
	 * Returns the sample associated with this aggregation, if any.
	 *
	 * @return An {@code Optional} containing the sample, or empty if no sample
	 *         is associated.
	 */
	public Optional<Sample> getSample()
	{
		return Optional.ofNullable(sample);
	}

	/**
	 * Sets the sample associated with this aggregation.
	 *
	 * @param sample
	 *            The sample to associate with this aggregation.
	 */
	public void setSample(Sample sample)
	{
		this.sample = sample;
	}

	/**
	 * Adds an evaluation result to this aggregation. The evaluation's name is
	 * used as the key and its value as the map value.
	 *
	 * @param evaluation
	 *            The evaluation to add.
	 * @return The previous value associated with the evaluation's name, or
	 *         {@code null} if there was no mapping.
	 */
	public Double put(Evaluation evaluation)
	{
		return put(evaluation.getName(), evaluation.getValue());
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
		if (!super.equals(o))
		{
			return false;
		}
		EvaluationAggregation that = (EvaluationAggregation) o;
		return Objects.equals(sample, that.sample);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(super.hashCode(), sample);
	}
}
