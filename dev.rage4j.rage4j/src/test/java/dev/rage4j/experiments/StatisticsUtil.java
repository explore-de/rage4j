package dev.rage4j.experiments;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.rage4j.evaluation.Evaluation;
import dev.rage4j.experiments.enity.ExperimentEvaluation;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StatisticsUtil
{
	private static final Logger LOGGER = LoggerFactory.getLogger(StatisticsUtil.class);

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

	public static Stats buildStats(Map.Entry<String, List<ExperimentEvaluation>> entry)
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

	public static void writeToFile(List<Stats> statsList, Map<String, List<ExperimentEvaluation>> results, String modelName, String filePrefix)
	{
		try
		{
			String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
			String filename = filePrefix + "_" + modelName + "_" + timestamp;
			Path outputDir = Paths.get("experiment_results");
			Files.createDirectories(outputDir);
			Path logFile = outputDir.resolve(filename + ".log");
			List<String> lines = new ArrayList<>();
			lines.add("Statistics Report - " + LocalDateTime.now() + " - Model: " + modelName);
			lines.add("=".repeat(80));
			lines.add("");
			for (Stats stats : statsList)
			{
				lines.add(stats.toString());
				lines.add("");
			}
			for (Map.Entry<String, List<ExperimentEvaluation>> entry : results.entrySet())
			{
				lines.add("Detailed Results for " + entry.getKey() + ":");
				lines.add("=".repeat(80));
				for (Evaluation eval : entry.getValue())
				{
					lines.add("Value: " + eval.getValue() + " | Explanations: " + eval.getExplanations());
				}
				lines.add("");
			}
			Files.write(logFile, lines, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
			LOGGER.info("Statistics written to file: {}", logFile.toAbsolutePath());
			// Also write detailed results as JSON
			ObjectMapper mapper = new ObjectMapper();
			Path jsonFile = outputDir.resolve(filename + ".json");
			mapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile.toFile(), results);
			LOGGER.info("Detailed results written to file: {}", jsonFile.toAbsolutePath());
		}
		catch (IOException e)
		{
			LOGGER.error("Failed to write statistics to file", e);
		}
	}
}
