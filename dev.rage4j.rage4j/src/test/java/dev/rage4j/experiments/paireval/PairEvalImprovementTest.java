package dev.rage4j.experiments.paireval;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.rage4j.evaluation.Evaluation;
import dev.rage4j.evaluation.paireval.PairEvalEvaluator;
import dev.rage4j.experiments.DialogLoader;
import dev.rage4j.experiments.enity.Dialog;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static dev.langchain4j.model.chat.Capability.RESPONSE_FORMAT_JSON_SCHEMA;

public class PairEvalImprovementTest
{
	private static final String OPEN_AI_KEY = System.getenv("OPEN_AI_KEY");
	private static final String[] MODELS = { "gpt-5.2", "gpt-4.1" };

	private static final String DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss").format(LocalDateTime.now());
	private static final Map<String, Map<String, List<Double>>> dialogToModelResult = new HashMap<>();

	public static ChatModel getOpenAIChatModel(String modelName)
	{
		return OpenAiChatModel.builder()
			.apiKey(OPEN_AI_KEY)
			.modelName(modelName)
			.supportedCapabilities(RESPONSE_FORMAT_JSON_SCHEMA)
			.strictJsonSchema(true)
			.temperature(1.0)
			.logRequests(true)
			.build();
	}

	@AfterEach
	void saveResults() throws IOException
	{
		File file = new File("./experiment_results/pair_eval_improvement_" + DATE_TIME + ".json");
		new ObjectMapper().writeValue(file, dialogToModelResult);
	}

	@ParameterizedTest(name = "Test #{index} - {0} - {1} - {2}")
	@MethodSource("contextDialogExampleProvider")
	void testAXCELContextSize(Dialog dialog, String model, int run)
	{
		// given
		ChatModel chatModel = getOpenAIChatModel(model);
		PairEvalEvaluator evaluatorNew = new PairEvalEvaluatorConsistentPrompt(chatModel);
		PairEvalEvaluator evaluator = new PairEvalEvaluator(chatModel);

		// when
		Evaluation resultNew = evaluatorNew.evaluate(dialog.getSample());
		Evaluation result = evaluator.evaluate(dialog.getSample());

		// then
		Map<String, List<Double>> modelToResult = dialogToModelResult.computeIfAbsent(dialog.path(), k -> new HashMap<>());
		List<Double> results = modelToResult.computeIfAbsent(model, k -> new java.util.ArrayList<>());
		results.add(result.getValue());
		List<Double> resultsNew = modelToResult.computeIfAbsent(model + "_new", k -> new java.util.ArrayList<>());
		resultsNew.add(resultNew.getValue());
	}

	private static Stream<Arguments> contextDialogExampleProvider()
	{
		List<Dialog> dialogs = List.of(new DialogLoader().loadDialogs());
		List<Integer> runs = List.of(1, 2, 3);

		return dialogs.stream()
			.flatMap(dialog -> Arrays.stream(MODELS)
				.flatMap(model -> runs.stream()
					.map(run -> Arguments.of(
						Named.of("dialog " + dialog.path(), dialog),
						Named.of("model " + model, model),
						Named.of("run " + run, run)))));
	}
}