package dev.rage4j.experiments.paireval;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.exception.UnresolvedModelServerException;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
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
	private static final String[] MODELS = { "gpt-5.2", "gpt-4.1", "ministral-3", "llama3.1:8b", "gemma3:12b" };
	//private static final String[] OLLAMA_MODELS = { "gemma3:12b", "qwen3:14b" };//, "ministral-3", "llama3.1:8b"

	private static final String DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss").format(LocalDateTime.now());
	private static final Map<String, Map<String, List<Double>>> dialogToModelResult = new HashMap<>();
	private static final Logger log = LoggerFactory.getLogger(PairEvalImprovementTest.class);

	private static ChatModel getChatModel(String modelName)
	{
		if (modelName.startsWith("gpt-"))
		{
			return getOpenAIChatModel(modelName);
		}
		else
		{
			return getOllamaChatModel(modelName);
		}
	}

	private static ChatModel getOpenAIChatModel(String modelName)
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

	private static ChatModel getOllamaChatModel(String model)
	{
		return OllamaChatModel.builder()
			.baseUrl("http://localhost:11434")
			.supportedCapabilities(RESPONSE_FORMAT_JSON_SCHEMA)
			.modelName(model)
			.temperature(1.0)
			.logRequests(true)
			.timeout(Duration.ofMinutes(30))
			.build();
	}

	@AfterEach
	void saveResults() throws IOException
	{
		File file = new File("./experiment_results/pair_eval_improvement_translated_spanish" + DATE_TIME + ".json");
		new ObjectMapper().writeValue(file, dialogToModelResult);
	}

	@ParameterizedTest(name = "Test #{index} - {0} - {1} - {2}")
	@MethodSource("contextDialogExampleProvider")
	void testPairEvalPrompt(Dialog dialog, String model, int run) throws InterruptedException
	{
		try
		{
			// given
			ChatModel chatModel = getChatModel(model);
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
		catch (UnresolvedModelServerException e)
		{
			log.error("Model server is not available for model {}. Skipping this test run.", model, e);
			Thread.sleep(5000); // Wait for 2 seconds before the next test run
			testPairEvalPrompt(dialog, model, run);
		}
	}

	private static Stream<Arguments> contextDialogExampleProvider()
	{
		List<Dialog> dialogs = List.of(new DialogLoader().loadDialogs());
		List<Integer> runs = List.of(1, 2, 3);

		return Arrays.stream(MODELS)
			.flatMap(model -> dialogs.stream()
				.flatMap(dialog -> runs.stream()
					.map(run -> Arguments.of(
						Named.of("dialog " + dialog.path(), dialog),
						Named.of("model " + model, model),
						Named.of("run " + run, run)))));
	}
}