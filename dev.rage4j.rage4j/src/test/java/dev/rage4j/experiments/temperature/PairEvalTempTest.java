package dev.rage4j.experiments.temperature;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.rage4j.LoggingTestWatcher;
import dev.rage4j.evaluation.Evaluation;
import dev.rage4j.evaluation.paireval.PairEvalEvaluator;
import dev.rage4j.experiments.DialogLoader;
import dev.rage4j.experiments.StatisticsUtil;
import dev.rage4j.experiments.enity.Dialog;
import dev.rage4j.experiments.enity.ExperimentEvaluation;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dev.langchain4j.model.chat.Capability.RESPONSE_FORMAT_JSON_SCHEMA;

@ExtendWith(LoggingTestWatcher.class)
public class PairEvalTempTest
{
	private static final int RUNS = 40;
	private static final String OPEN_AI_MODEL_NAME = "gpt-5.1";
	private static final String OLLAMA_MODEL_NAME = "gemma3:12b";

	private static final String MODEL_NAME = OLLAMA_MODEL_NAME;
	private static final String OPEN_AI_KEY = System.getenv("OPEN_AI_KEY");
	private static final String DGX_AI_KEY = System.getenv("DGX_AI_KEY");
	private static final Map<String, List<ExperimentEvaluation>> RESULTS = new HashMap<>();
	private static final Logger LOGGER = LoggerFactory.getLogger(PairEvalTempTest.class);
	private static final DialogLoader DIALOG_LOADER = new DialogLoader();

	private static OpenAiChatModel getOpenAIChatModel(double temperature)
	{
		return OpenAiChatModel.builder()
			.apiKey(OPEN_AI_KEY)
			.modelName(MODEL_NAME)
			.supportedCapabilities(RESPONSE_FORMAT_JSON_SCHEMA)
			.strictJsonSchema(true)
			.temperature(temperature)
			.build();
	}

	private static ChatModel getDGXChatModel(double temperature)
	{
		return OpenAiChatModel.builder()
			.baseUrl("https://brev.explorelora.dev/v1")
			.customHeaders(Map.of("Authorization", "Bearer " + DGX_AI_KEY))
			.modelName(MODEL_NAME)
			.supportedCapabilities(RESPONSE_FORMAT_JSON_SCHEMA)
			.strictJsonSchema(true)
			.temperature(temperature)
			.timeout(Duration.ofMinutes(15))
			.build();
	}

	private static OllamaChatModel getOllamaChatModel(double temperature)
	{
		return OllamaChatModel.builder()
			.baseUrl("http://localhost:11434")
			.supportedCapabilities(RESPONSE_FORMAT_JSON_SCHEMA)
			.modelName(MODEL_NAME)
			.temperature(temperature)
			.timeout(Duration.ofMinutes(30))
			.customHeaders(Map.of("keep_alive", "30min"))
			.build();
	}

	@AfterAll
	static void afterAll()
	{
		LOGGER.info("Evaluation results over tests:");
		List<StatisticsUtil.Stats> statsList = RESULTS.entrySet().stream()
			.map(StatisticsUtil::buildStats)
			.toList();
		statsList.forEach(stats -> LOGGER.info("{}", stats));
		StatisticsUtil.writeToFile(statsList, RESULTS, MODEL_NAME, "paireval-temp");
	}

	@RepeatedTest(RUNS)
	@Tag("integration")
	void temperature0Evaluation()
	{
		Dialog dialog = DIALOG_LOADER.getRawDialog();

		runTestWithTemperature(1, dialog);
		runTestWithTemperature(0, dialog);
	}

	private void runTestWithTemperature(int temperature, Dialog dialog)
	{
		// given
		PairEvalEvaluator evaluator = new PairEvalEvaluator(getOllamaChatModel(temperature));

		// when
		Evaluation evaluation = evaluator.evaluate(dialog.getSample());
		ExperimentEvaluation experimentEvaluation = new ExperimentEvaluation(evaluation, dialog.path());

		// then
		String mapKey = "temperature-" + temperature;
		RESULTS.computeIfAbsent(mapKey, k -> new ArrayList<>()).add(experimentEvaluation);
	}
}