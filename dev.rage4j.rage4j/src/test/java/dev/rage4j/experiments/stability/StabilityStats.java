package dev.rage4j.experiments.stability;

import java.util.ArrayList;
import java.util.List;

final class StabilityStats
{
	public final int count;
	public final double mean;
	public final double stdev;
	public final double min;
	public final double max;
	public final double median;
	public final double p25;
	public final double p75;

	private StabilityStats(int count, double mean, double stdev, double min, double max, double median, double p25, double p75)
	{
		this.count = count;
		this.mean = mean;
		this.stdev = stdev;
		this.min = min;
		this.max = max;
		this.median = median;
		this.p25 = p25;
		this.p75 = p75;
	}

	static StabilityStats from(List<Double> values)
	{
		if (values == null || values.isEmpty())
		{
			return new StabilityStats(0, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN);
		}
		List<Double> sorted = new ArrayList<>(values);
		sorted.sort(Double::compareTo);
		int n = sorted.size();
		double sum = 0.0;
		for (double v : sorted)
		{
			sum += v;
		}
		double mean = sum / n;
		double varianceSum = 0.0;
		for (double v : sorted)
		{
			double diff = v - mean;
			varianceSum += diff * diff;
		}
		double stdev = n > 1 ? Math.sqrt(varianceSum / (n - 1)) : 0.0;
		double min = sorted.get(0);
		double max = sorted.get(n - 1);
		double median = percentile(sorted, 0.5);
		double p25 = percentile(sorted, 0.25);
		double p75 = percentile(sorted, 0.75);
		return new StabilityStats(n, mean, stdev, min, max, median, p25, p75);
	}

	private static double percentile(List<Double> sorted, double p)
	{
		int n = sorted.size();
		if (n == 0)
		{
			return Double.NaN;
		}
		double pos = p * (n - 1);
		int lower = (int)Math.floor(pos);
		int upper = (int)Math.ceil(pos);
		if (lower == upper)
		{
			return sorted.get(lower);
		}
		double weight = pos - lower;
		return sorted.get(lower) * (1.0 - weight) + sorted.get(upper) * weight;
	}
}
