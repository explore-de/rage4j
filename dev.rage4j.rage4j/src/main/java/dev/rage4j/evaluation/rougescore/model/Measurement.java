package dev.rage4j.evaluation.rougescore.model;

public class Measurement
{
	private final double precision;
	private final double recall;

	public Measurement(double precision, double recall)
	{
		this.precision = precision;
		this.recall = recall;
	}

	public double getPrecision()
	{
		return precision;
	}

	public double getRecall()
	{
		return recall;
	}
}
