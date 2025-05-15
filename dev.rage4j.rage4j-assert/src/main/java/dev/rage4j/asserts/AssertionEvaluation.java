package dev.rage4j.asserts;

import dev.rage4j.evaluation.Evaluation;

public class AssertionEvaluation
{
	private final RageAssertTestCaseAssertions assertions;
	private final Evaluation evaluation;

	/**
	 * Constructs a new {@code Evaluation} object with the specified metric name
	 * and value.
	 *
	 * @param evaluation
	 *            The evaluation containing metric name and evaluated value
	 * @param assertions
	 *            The assertions this evaluation can concatenate
	 */
	private AssertionEvaluation(Evaluation evaluation, RageAssertTestCaseAssertions assertions)
	{
		this.evaluation = evaluation;
		this.assertions = assertions;
	}

	public RageAssertTestCaseAssertions then()
	{
		return assertions;
	}

	public Evaluation getEvaluation()
	{
		return evaluation;
	}

	public static AssertionEvaluation from(Evaluation evaluation, RageAssertTestCaseAssertions assertions)
	{
		return new AssertionEvaluation(evaluation, assertions);
	}
}