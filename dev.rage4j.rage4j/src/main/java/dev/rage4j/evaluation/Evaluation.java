package dev.rage4j.evaluation;

/**
 * The {@code Evaluation} class represents the result of evaluating a particular
 * metric. It stores the name of the metric and its calculated value.
 * <p>
 * This class is immutable, meaning its fields cannot be changed after
 * construction.
 */
public class Evaluation
{
	private final String name;
	private final double value;

	/**
	 * Constructs a new {@code Evaluation} object with the specified metric name
	 * and value.
	 *
	 * @param name
	 *            The name of the metric being evaluated.
	 * @param value
	 *            The calculated value of the evaluation.
	 */
	public Evaluation(String name, double value)
	{
		this.name = name;
		this.value = value;
	}

	/**
	 * Returns the name of the metric that was evaluated.
	 *
	 * @return The name of the metric.
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Returns the value of the evaluated metric.
	 *
	 * @return The evaluation value.
	 */
	public double getValue()
	{
		return value;
	}
}
