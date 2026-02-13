package dev.rage4j.experiments.context;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.rage4j.evaluation.Evaluation;
import dev.rage4j.evaluation.paireval.PairEvalEvaluator;
import dev.rage4j.experiments.DialogLoader;
import dev.rage4j.experiments.StatisticsUtil;
import dev.rage4j.experiments.enity.Dialog;
import dev.rage4j.experiments.enity.ExperimentEvaluation;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static dev.langchain4j.model.chat.Capability.RESPONSE_FORMAT_JSON_SCHEMA;

public class PairEvalContextTest
{
	private static final List<String> MODELS = List.of("ministral-3", "gemma3:12b", "llama3.1:8b", "qwen3:14b");
	private static final int[] CONTEXT_SIZES = { 2048, 4096, 8192 };

	private static final Map<String, ModelResults> MODEL_RESULTS_MAP = new HashMap<>();

	private static final Logger LOGGER = LoggerFactory.getLogger(PairEvalContextTest.class);

	protected static OllamaChatModel getOllamaChatModel(String model, int context)
	{
		return OllamaChatModel.builder()
			.baseUrl("http://localhost:11434")
			.supportedCapabilities(RESPONSE_FORMAT_JSON_SCHEMA)
			.modelName(model)
			.temperature(1.0)
			.numCtx(context)
			.timeout(Duration.ofMinutes(30))
			.build();
	}

	@AfterAll
	static void afterAll()
	{
		MODEL_RESULTS_MAP.values().forEach(ModelResults::storeResults);
	}

	@ParameterizedTest(name = "Test #{index} - {0} - {1} - {2}")
	@MethodSource("contextDialogExampleProvider")
	void testPairEvalContextSize(String model, int context, Dialog dialog, int run)
	{
		try
		{
			ExperimentEvaluation experimentEvaluation = getExperimentEvaluation(model, context, dialog);
			MODEL_RESULTS_MAP.computeIfAbsent(model, ModelResults::new).addExperimentEvaluation(context, experimentEvaluation, run);
		}
		catch (Exception ex)
		{
			MODEL_RESULTS_MAP.computeIfAbsent(model, ModelResults::new).addError(context, dialog.path());
			LOGGER.error(ex.getMessage(), ex);
			throw ex;
		}
	}

	protected @NonNull ExperimentEvaluation getExperimentEvaluation(String model, int context, Dialog dialog)
	{
		// given
		ChatModel chatModel = getOllamaChatModel(model, context);
		PairEvalEvaluator evaluator = new PairEvalEvaluator(chatModel);

		// when
		Evaluation evaluation = evaluator.evaluate(dialog.getSample());
		return new ExperimentEvaluation(evaluation, dialog.path());
	}

	private static Stream<Arguments> contextDialogExampleProvider()
	{
		List<Dialog> dialogs = List.of(new DialogLoader().loadDialogs());
		List<Integer> runs = List.of(1, 2, 3);

		return IntStream.of(CONTEXT_SIZES)
			.boxed()
			.flatMap(context -> MODELS.stream()
				.flatMap(model -> dialogs.stream()
					.flatMap(dialog -> runs.stream()
						.map(run -> Arguments.of(
							Named.of("model " + model, model),
							Named.of("context " + context, context),
							Named.of("dialog " + dialog.path(), dialog),
							Named.of("run " + run, run))))));
	}
}

class ModelResults
{
	public static final String FILE_PREFIX = "paireval-context";
	public final Map<String, List<ExperimentEvaluation>> CONTEXT_EXAMPLE_RESULTS = new HashMap<>();
	public final Map<String, List<ExperimentEvaluation>> CONTEXT_RESULTS = new HashMap<>();
	public final Map<String, List<String>> ERRORS_BY_CONTEXT = new HashMap<>();
	public final String modelName;

	ModelResults(String modelName)
	{
		this.modelName = modelName;
	}

	public void addExperimentEvaluation(int context, ExperimentEvaluation experimentEvaluation, int run)
	{
		String mapKey = "context-" + context + "-run-" + run;
		CONTEXT_EXAMPLE_RESULTS.computeIfAbsent(mapKey, k -> new ArrayList<>()).add(experimentEvaluation);
		mapKey = "context-" + context;
		CONTEXT_RESULTS.computeIfAbsent(mapKey, k -> new ArrayList<>()).add(experimentEvaluation);
	}

	public void addError(int context, String dialogPath)
	{
		String mapKey = "context-" + context;
		ERRORS_BY_CONTEXT.computeIfAbsent(mapKey, k -> new ArrayList<>()).add(dialogPath);
	}

	public void storeResults()
	{        // Grouped by context, dialog and one shot example
		List<StatisticsUtil.Stats> statsList = CONTEXT_EXAMPLE_RESULTS.entrySet().stream()
			.map(StatisticsUtil::buildStats)
			.toList();
		StatisticsUtil.writeToFile(statsList, CONTEXT_EXAMPLE_RESULTS, modelName, FILE_PREFIX);

		// Grouped by context and dialog only
		statsList = CONTEXT_RESULTS.entrySet().stream()
			.map(StatisticsUtil::buildStats)
			.toList();
		StatisticsUtil.writeToCSV(CONTEXT_RESULTS, modelName + "_reduced", FILE_PREFIX);
		StatisticsUtil.writeToFile(statsList, CONTEXT_RESULTS, modelName + "_reduced", FILE_PREFIX);

		// Store errors by context
		try
		{
			new ObjectMapper().writeValue(new File("./experiment_results/" + FILE_PREFIX + "_" + modelName + "_errors.json"), ERRORS_BY_CONTEXT);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
}