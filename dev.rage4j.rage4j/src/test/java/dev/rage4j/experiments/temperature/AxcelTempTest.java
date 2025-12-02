package dev.rage4j.experiments.temperature;

import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.rage4j.LoggingTestWatcher;
import dev.rage4j.evaluation.Evaluation;
import dev.rage4j.evaluation.axcel.AxcelEvaluator;
import dev.rage4j.experiments.DialogLoader;
import dev.rage4j.experiments.StatisticsUtil;
import dev.rage4j.model.Sample;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dev.langchain4j.model.chat.Capability.RESPONSE_FORMAT_JSON_SCHEMA;

@ExtendWith(LoggingTestWatcher.class)
public class AxcelTempTest
{
	private static final String OPEN_AI_KEY = System.getenv("OPEN_AI_KEY");
	private static final Map<String, List<Double>> RESULTS = new HashMap<>();
	private static final int RUNS = 10;
	private static final Logger LOGGER = LoggerFactory.getLogger(AxcelTempTest.class);
	private static final DialogLoader DIALOG_LOADER = new DialogLoader();

	private AxcelEvaluator evaluator;

	private static OpenAiChatModel buildChatModel(double temperature)
	{
		return OpenAiChatModel.builder()
			.apiKey(OPEN_AI_KEY)
			.modelName("gpt-4.1")
			.supportedCapabilities(RESPONSE_FORMAT_JSON_SCHEMA)
			.strictJsonSchema(true)
			.temperature(temperature)
			.build();
	}

	@AfterAll
	static void afterAll()
	{
		LOGGER.info("Evaluation results over tests:");
		RESULTS.entrySet().stream()
			.map(StatisticsUtil::buildStats)
			.forEach(stats -> LOGGER.info("{}", stats));
	}

	@RepeatedTest(RUNS)
	@Tag("integration")
	void temperature0Evaluation()
	{
		Sample sample = DIALOG_LOADER.getDialog();
		runTestWithTemperature(1, sample);
		runTestWithTemperature(0, sample);
	}

	private void runTestWithTemperature(int temperature, Sample sample)
	{
		// given
		evaluator = new AxcelEvaluator(buildChatModel(temperature));

		// when
		Evaluation evaluation = evaluator.evaluate(sample);

		// then
		String mapKey = "temperature" + temperature;
		RESULTS.computeIfAbsent(mapKey, k -> new ArrayList<>()).add(evaluation.getValue());
	}
}