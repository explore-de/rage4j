package dev.rage4j.experiments.temperature;

import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.rage4j.LoggingTestWatcher;
import dev.rage4j.evaluation.Evaluation;
import dev.rage4j.evaluation.axcel.AxcelDataLoader;
import dev.rage4j.evaluation.axcel.AxcelEvaluator;
import dev.rage4j.evaluation.axcel.AxcelOneShotExamples;
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
	private static final Map<String, List<Evaluation>> RESULTS = new HashMap<>();
	private static final int RUNS = 20;
	private static final Logger LOGGER = LoggerFactory.getLogger(AxcelTempTest.class);
	private static final DialogLoader DIALOG_LOADER = new DialogLoader();
	private final AxcelDataLoader loader = new AxcelDataLoader();

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
		List<StatisticsUtil.Stats> statsList = RESULTS.entrySet().stream()
			.map(StatisticsUtil::buildStats)
			.toList();
		statsList.forEach(stats -> LOGGER.info("{}", stats));
		// Write each statistic to a log file
		// statsList.forEach();
	}

	@RepeatedTest(RUNS)
	@Tag("integration")
	void temperature0Evaluation()
	{
		Sample sample = DIALOG_LOADER.getDialog();
		AxcelOneShotExamples oneShotExample = loader.loadExampleData();
		runTestWithTemperature(1, sample, oneShotExample);
		runTestWithTemperature(0, sample, oneShotExample);
	}

	private void runTestWithTemperature(int temperature, Sample sample, AxcelOneShotExamples oneShotExample)
	{
		// given
		AxcelEvaluator evaluator = new AxcelEvaluator(buildChatModel(temperature));

		// when
		Evaluation evaluation = evaluator.evaluate(sample, oneShotExample);

		// then
		String mapKey = "temperature-" + temperature;
		RESULTS.computeIfAbsent(mapKey, k -> new ArrayList<>()).add(evaluation);
	}
}