package dev.rage4j.experiments.context;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.rage4j.evaluation.Evaluation;
import dev.rage4j.evaluation.axcel.AxcelDataLoader;
import dev.rage4j.evaluation.axcel.AxcelEvaluator;
import dev.rage4j.evaluation.axcel.AxcelOneShotExamples;
import dev.rage4j.experiments.DialogLoader;
import dev.rage4j.experiments.StatisticsUtil;
import dev.rage4j.experiments.enity.Dialog;
import dev.rage4j.experiments.enity.ExperimentEvaluation;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dev.langchain4j.model.chat.Capability.RESPONSE_FORMAT_JSON_SCHEMA;

public class AxcelContextTest
{
	private static final String MODEL_NAME = "gemma3:12b";
	private static final int START_CONTEXT_SIZE = 2048;

	private static final Map<String, List<ExperimentEvaluation>> CONTEXT_EXAMPLE_RESULTS = new HashMap<>();
	private static final Map<String, List<ExperimentEvaluation>> CONTEXT_RESULTS = new HashMap<>();
	private static final Logger LOGGER = LoggerFactory.getLogger(AxcelContextTest.class);
	private static final DialogLoader DIALOG_LOADER = new DialogLoader();

	private final AxcelDataLoader loader = new AxcelDataLoader();

	private static OllamaChatModel getOllamaChatModel(int context)
	{
		return OllamaChatModel.builder()
			.baseUrl("http://localhost:11434")
			.supportedCapabilities(RESPONSE_FORMAT_JSON_SCHEMA)
			.modelName(MODEL_NAME)
			.temperature(1.0)
			.numCtx(context)
			.timeout(Duration.ofMinutes(30))
			.build();
	}

	@AfterAll
	static void afterAll()
	{
		// Grouped by context, dialog and one shot example
		List<StatisticsUtil.Stats> statsList = CONTEXT_EXAMPLE_RESULTS.entrySet().stream()
			.map(StatisticsUtil::buildStats)
			.toList();
		StatisticsUtil.writeToFile(statsList, CONTEXT_EXAMPLE_RESULTS, MODEL_NAME, "axcel-context");

		// Grouped by context and dialog only
		statsList = CONTEXT_RESULTS.entrySet().stream()
			.map(StatisticsUtil::buildStats)
			.toList();
		StatisticsUtil.writeToCSV(CONTEXT_RESULTS, MODEL_NAME + "_reduced", "axcel-context");
		StatisticsUtil.writeToFile(statsList, CONTEXT_RESULTS, MODEL_NAME + "_reduced", "axcel-context");

		LOGGER.info("Evaluation results over tests:");
		statsList.forEach(stats -> LOGGER.info("{}", stats));
	}

	@ParameterizedTest(name = "Test #{index} - Context Size: {0}")
	@ValueSource(ints = { 2048, 4096, 8192 })
	void testAxcelContextSize(int context)
	{
		ChatModel chatModel = getOllamaChatModel(context);

		for (Dialog dialog : DIALOG_LOADER.loadDialogs())
		{
			for (AxcelOneShotExamples example : loader.loadAllExampleData())
			{
				runEvaluation(dialog, chatModel, example, context);
			}
		}
	}

	private static void runEvaluation(Dialog dialog, ChatModel chatModel, AxcelOneShotExamples oneShotExample, int context)
	{
		try
		{
			// given
			AxcelEvaluator evaluator = new AxcelEvaluator(chatModel);

			// when
			Evaluation evaluation = evaluator.evaluate(dialog.getSample(), oneShotExample);
			ExperimentEvaluation experimentEvaluation = new ExperimentEvaluation(evaluation, dialog.path());

			// then
			String mapKey = "context-" + context + "-example-" + oneShotExample.hashCode();
			CONTEXT_EXAMPLE_RESULTS.computeIfAbsent(mapKey, k -> new ArrayList<>()).add(experimentEvaluation);
			mapKey = "context-" + context;
			CONTEXT_RESULTS.computeIfAbsent(mapKey, k -> new ArrayList<>()).add(experimentEvaluation);
		}
		catch (Exception e)
		{
			LOGGER.error("Evaluation failed for dialog: {} with context size: {}", dialog.path(), context, e);
		}
	}
}
