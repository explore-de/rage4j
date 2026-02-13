package dev.rage4j.experiments.context;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.rage4j.evaluation.Evaluation;
import dev.rage4j.evaluation.axcel.AxcelDataLoader;
import dev.rage4j.evaluation.axcel.AxcelEvaluator;
import dev.rage4j.evaluation.axcel.AxcelOneShotExamples;
import dev.rage4j.experiments.DialogLoader;
import dev.rage4j.experiments.enity.Dialog;
import dev.rage4j.experiments.enity.ExperimentEvaluation;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static dev.langchain4j.model.chat.Capability.RESPONSE_FORMAT_JSON_SCHEMA;

public class AxcelContextTest
{
	private static final List<String> MODELS = List.of("ministral-3", "gemma3:12b", "llama3.1:8b", "qwen3:14b");
	private static final int[] CONTEXT_SIZES = { 2048, 4096, 8192 };

	private static final Map<String, AxcelModelResults> MODEL_RESULTS_MAP = new HashMap<>();
	private static final Logger LOGGER = LoggerFactory.getLogger(AxcelContextTest.class);
	private static final AxcelDataLoader AXCEL_DATA_LOADER = new AxcelDataLoader();

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

	@AfterEach
	void saveResults()
	{
		MODEL_RESULTS_MAP.values().forEach(ModelResults::storeResults);
	}

	@ParameterizedTest(name = "Test #{index} - {0} - {1} - {2}")
	@MethodSource("contextDialogExampleProvider")
	void testAXCELContextSize(String model, int context, Dialog dialog, AxcelOneShotExamples oneShotExample)
	{
		try
		{
			ExperimentEvaluation experimentEvaluation = getExperimentEvaluation(model, context, dialog, oneShotExample);
			MODEL_RESULTS_MAP.computeIfAbsent(model, AxcelModelResults::new).addExperimentEvaluation(context, experimentEvaluation, oneShotExample);
		}
		catch (Exception ex)
		{
			MODEL_RESULTS_MAP.computeIfAbsent(model, AxcelModelResults::new).addError(context, dialog.path());
			LOGGER.error(ex.getMessage(), ex);
			throw ex;
		}
	}

	protected @NonNull ExperimentEvaluation getExperimentEvaluation(String model, int context, Dialog dialog, AxcelOneShotExamples oneShotExample)
	{
		// given
		ChatModel chatModel = getOllamaChatModel(model, context);
		AxcelEvaluator evaluator = new AxcelEvaluator(chatModel);

		// when
		Evaluation evaluation = evaluator.evaluate(dialog.getSample(), oneShotExample);
		return new ExperimentEvaluation(evaluation, dialog.path());
	}

	private static Stream<Arguments> contextDialogExampleProvider()
	{
		List<Dialog> dialogs = List.of(new DialogLoader().loadDialogs()[0]);
		List<AxcelOneShotExamples> examples = AXCEL_DATA_LOADER.loadAllExampleData();

		return IntStream.of(CONTEXT_SIZES)
			.boxed()
			.flatMap(context -> MODELS.stream()
				.flatMap(model -> dialogs.stream()
					.flatMap(dialog -> examples.stream()
						.map(example -> Arguments.of(
							Named.of("model " + model, model),
							Named.of("context " + context, context),
							Named.of("dialog " + dialog.path(), dialog),
							Named.of("examples " + example.hashCode(), example))))));
	}
}
