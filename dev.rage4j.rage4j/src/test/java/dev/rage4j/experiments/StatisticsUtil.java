package dev.rage4j.experiments;

import dev.rage4j.evaluation.Evaluation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StatisticsUtil
{
	public record Stats(
		String key,
		List<Double> values,
		double average, double min,
		double max, double median,
		double stdDev,
		double variance)
	{

		@Override
		public @NotNull String toString()
		{
			return String.format("  %s:%n    Values: %s%n    Average: %.4f%n    Median:  %.4f%n    Min:     %.4f%n    Max:     %.4f%n    StdDev:  %.4f%n    Variance:%.4f",
				key, values, average, median, min, max, stdDev, variance);
		}
	}

	public static Stats buildStats(Map.Entry<String, List<Evaluation>> entry)
	{
		// Extracts the values from the Evaluation objects
		List<Double> valueList = entry.getValue().stream().map(Evaluation::getValue).toList();
		return buildStats(entry.getKey(), valueList);
	}

	public static @NotNull Stats buildStats(String key, List<Double> value)
	{
		double average = value.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
		double min = value.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
		double max = value.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
		double median = calculateMedian(value);
		// Standard Abweichung
		double stdDev = calculateStdDev(value, average);
		double variance = stdDev * stdDev;

		return new Stats(key, value, average, min, max, median, stdDev, variance);
	}

	private static double calculateMedian(List<Double> values)
	{
		if (values.isEmpty()) return 0.0;
		List<Double> sorted = new ArrayList<>(values);
		sorted.sort(Double::compareTo);
		int size = sorted.size();
		if (size % 2 == 0)
		{
			return (sorted.get(size / 2 - 1) + sorted.get(size / 2)) / 2.0;
		}
		else
		{
			return sorted.get(size / 2);
		}
	}

	private static double calculateStdDev(List<Double> values, double mean)
	{
		if (values.isEmpty()) return 0.0;
		double sumSquaredDiff = values.stream()
			.mapToDouble(v -> Math.pow(v - mean, 2))
			.sum();
		return Math.sqrt(sumSquaredDiff / values.size());
	}
}
