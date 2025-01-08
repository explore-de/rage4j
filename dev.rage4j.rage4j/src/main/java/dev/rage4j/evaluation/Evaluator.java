package dev.rage4j.evaluation;

import dev.rage4j.model.Sample;

/**
 * The {@code Evaluator} interface defines a contract for evaluating a sample
 * based on a particular metric. Implementing classes should provide the logic
 * to assess a {@code Sample} and return an {@code Evaluation} result.
 */
public interface Evaluator
{
	/**
	 * Evaluates the given sample according to a specific metric and returns the
	 * result as an {@code Evaluation}.
	 *
	 * @param sample
	 *            The sample containing data (such as context and answer) to be
	 *            evaluated.
	 * @return An {@code Evaluation} object representing the metric name and its
	 *         calculated value.
	 * @throws IllegalArgumentException
	 *             if the sample is invalid or cannot be evaluated.
	 */
	Evaluation evaluate(Sample sample);
}
