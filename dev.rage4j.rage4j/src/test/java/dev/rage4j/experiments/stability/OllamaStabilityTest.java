package dev.rage4j.experiments.stability;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.rage4j.evaluation.Evaluation;
import dev.rage4j.evaluation.axcel.AxcelDataLoader;
import dev.rage4j.evaluation.axcel.AxcelEvaluator;
import dev.rage4j.evaluation.axcel.AxcelOneShotExamples;
import dev.rage4j.experiments.DialogLoader;
import dev.rage4j.experiments.enity.Dialog;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dev.langchain4j.model.chat.Capability.RESPONSE_FORMAT_JSON_SCHEMA;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class OllamaStabilityTest
{
	private static final DialogLoader DIALOG_LOADER = new DialogLoader();
	private static final String NON_THINKING_MODEL = "gemma3:12b";

	private static final int RUNS = 1;
	private static final Map<String, List<Double>> runRatings = new HashMap<>();

	private static final String STATS_DIR = "stability";
	private static final String TIMESTAMP = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
	private static final String STATS_FILE = "ollama_stability_stats_" + TIMESTAMP;
	private static final Logger LOGGER = LoggerFactory.getLogger(OllamaStabilityTest.class);

	private static AxcelOneShotExamples axcelOneShotExamples;

	@AfterAll
	static void afterAll()
	{
		try
		{
			java.nio.file.Path dir = java.nio.file.Paths.get(STATS_DIR);
			java.nio.file.Files.createDirectories(dir);
			java.nio.file.Path out = dir.resolve(STATS_FILE + ".csv");
			try (java.io.BufferedWriter writer = java.nio.file.Files.newBufferedWriter(out))
			{
				writer.write("dialog_path,count,mean,stdev,min,max,median,p25,p75\n");
				runRatings.forEach((path, values) -> {
					StabilityStats stats = StabilityStats.from(values);
					try
					{
						writer.write(String.format(java.util.Locale.ROOT,
							"%s,%d,%.6f,%.6f,%.6f,%.6f,%.6f,%.6f,%.6f%n",
							escapeCsv(path),
							stats.count,
							stats.mean,
							stats.stdev,
							stats.min,
							stats.max,
							stats.median,
							stats.p25,
							stats.p75));
					}
					catch (java.io.IOException e)
					{
						throw new java.io.UncheckedIOException(e);
					}
				});
			}
			System.out.println("Wrote stability stats to: " + out);
			// Also save map as JSON
			ObjectMapper mapper = new ObjectMapper();
			Path jsonFile = dir.resolve(STATS_FILE + ".json");
			mapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile.toFile(), runRatings);
			LOGGER.info("Detailed results written to file: {}", jsonFile.toAbsolutePath());
		}
		catch (java.io.IOException e)
		{
			System.err.println("Failed to write stability stats: " + e.getMessage());
		}
	}

	@BeforeAll
	static void beforeAll()
	{
		axcelOneShotExamples = new AxcelDataLoader().loadExampleData();
	}

	@RepeatedTest(RUNS)
	void ollamaAxcelEvaluation()
	{
		for (Dialog dialog : DIALOG_LOADER.loadDialogs())
		{
			try
			{
				// given
				AxcelEvaluator evaluator = new AxcelEvaluator(getChatModel());

				// when
				Evaluation evaluation = evaluator.evaluate(dialog.getSample(), axcelOneShotExamples);

				// then
				assertNotNull(evaluation);
				System.out.printf("Evaluation result: %s = %.2f%n\n%s", evaluation.getName(), evaluation.getValue(), evaluation.getExplanations());
				System.out.println("Dialog: " + dialog.path());

				runRatings.computeIfAbsent(dialog.path(), k -> new ArrayList<>()).add(evaluation.getValue());
			}
			catch (Exception e)
			{
				System.err.printf("Error during evaluation of dialog %s: %s%n", dialog.path(), e.getMessage());
			}
		}
	}

	private static OllamaChatModel getChatModel()
	{
		return OllamaChatModel.builder()
			.baseUrl("http://localhost:11434")
			.supportedCapabilities(RESPONSE_FORMAT_JSON_SCHEMA)
			.modelName(NON_THINKING_MODEL)
			.logResponses(true)
			.temperature(1.0)
			.build();
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
}
