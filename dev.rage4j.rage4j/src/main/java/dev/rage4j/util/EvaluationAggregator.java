package dev.rage4j.util;

import dev.rage4j.evaluation.Evaluator;
import dev.rage4j.model.EvaluationAggregation;
import dev.rage4j.model.Sample;

/**
 * The {@code EvaluationAggregator} class provides utility methods for
 * aggregating multiple evaluations. This class takes a sample and a series of
 * evaluators, applies each evaluator to the sample, and aggregates the results
 * into an {@code EvaluationAggregation} object.
 */
public class EvaluationAggregator
{

	private EvaluationAggregator()
	{
		throw new IllegalStateException("EvaluationAggregator is a utility class and should not be instantiated.");
	}

	/**
	 * Evaluates the provided {@code Sample} using all the specified
	 * {@code Evaluator} objects and aggregates the results. Each evaluator
	 * produces an {@code Evaluation}, which is added to the aggregation. The
	 * aggregation contains a map of metric names to their corresponding
	 * evaluation values.
	 *
	 * @param sample
	 *            The sample containing the data to be evaluated.
	 * @param evaluators
	 *            The evaluators used to evaluate the sample.
	 * @return An {@code EvaluationAggregation} containing the results of all
	 *         evaluations.
	 * @throws IllegalArgumentException
	 *             if any evaluator fails to evaluate the sample.
	 */
	public static EvaluationAggregation evaluateAll(Sample sample, Evaluator... evaluators)
	{
		EvaluationAggregation aggregation = new EvaluationAggregation(sample);
		for (Evaluator evaluator : evaluators)
		{
			aggregation.put(evaluator.evaluate(sample));
		}
		return aggregation;
	}
}
