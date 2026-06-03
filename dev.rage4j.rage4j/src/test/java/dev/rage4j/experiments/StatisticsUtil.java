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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

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

	public static void writeToCSV(Map<String, List<ExperimentEvaluation>> results, String modelName, String filePrefix)
	{
		// header
		// <map key aka context>, <dialogFile>_score, ...
		if (results == null || results.isEmpty())
		{
			LOGGER.warn("No results to write to CSV.");
			return;
		}

		Set<String> dialogFiles = new TreeSet<>();
		for (List<ExperimentEvaluation> evaluations : results.values())
		{
			for (ExperimentEvaluation evaluation : evaluations)
			{
				dialogFiles.add(evaluation.getDialogFile());
			}
		}

		String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
		Path outputDir = Paths.get("experiment_results");
		String escapedModelName = modelName.replace(":", "-").replace("/", "-");
		String filename = filePrefix + "_" + escapedModelName + "_" + timestamp + ".csv";
		Path csvFile = outputDir.resolve(filename);

		List<String> lines = new ArrayList<>();
		StringBuilder header = new StringBuilder("context");
		for (String dialogFile : dialogFiles)
		{
			header.append(",").append(escapeCsv(dialogFile + "_score"));
		}
		lines.add(header.toString());

		Map<String, List<ExperimentEvaluation>> sortedResults = new TreeMap<>(results);
		for (Map.Entry<String, List<ExperimentEvaluation>> entry : sortedResults.entrySet())
		{
			Map<String, List<Double>> valuesByDialog = new HashMap<>();
			for (ExperimentEvaluation evaluation : entry.getValue())
			{
				valuesByDialog.computeIfAbsent(evaluation.getDialogFile(), k -> new ArrayList<>())
					.add(evaluation.getValue());
			}

			StringBuilder row = new StringBuilder(escapeCsv(entry.getKey()));
			for (String dialogFile : dialogFiles)
			{
				List<Double> values = valuesByDialog.get(dialogFile);
				row.append(",");
				if (values == null || values.isEmpty())
				{
					continue;
				}
				double value = values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
				row.append(String.format(Locale.ROOT, "%.6f", value));
			}
			lines.add(row.toString());
		}

		try
		{
			Files.createDirectories(outputDir);
			Files.write(csvFile, lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
			LOGGER.info("Statistics written to CSV: {}", csvFile.toAbsolutePath());
		}
		catch (IOException e)
		{
			LOGGER.error("Failed to write statistics to CSV", e);
		}
	}

	private static String escapeCsv(String value)
	{
		if (value == null)
		{
			return "";
		}
		boolean needsQuotes = value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r");
		String escaped = value.replace("\"", "\"\"");
		return needsQuotes ? "\"" + escaped + "\"" : escaped;
	}

	public static void writeToFile(List<Stats> statsList, Map<String, List<ExperimentEvaluation>> results, String modelName, String filePrefix)
	{
		try
		{
			String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
			String escapedModelName = modelName.replace(":", "-").replace("/", "-");
			String filename = filePrefix + "_" + escapedModelName + "_" + timestamp;
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
