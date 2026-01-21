package dev.rage4j.experiments;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.rage4j.evaluation.Evaluation;
import dev.rage4j.evaluation.axcel.AxcelDataLoader;
import dev.rage4j.evaluation.axcel.AxcelEvaluator;
import dev.rage4j.experiments.enity.Dialog;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static dev.langchain4j.model.chat.Capability.RESPONSE_FORMAT_JSON_SCHEMA;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class OllamaSetupTest
{
	private static final DialogLoader DIALOG_LOADER = new DialogLoader();
	private static final String NON_THINKING_MODEL = "ministral-3:3b";
	private static final String THINKING_MODEL = "qwen3:4b";

	@Tag("integration")
	@ParameterizedTest()
	@ValueSource(strings = { NON_THINKING_MODEL, THINKING_MODEL })
	void ollamaEvaluation(String model)
	{
		// given
		ChatModel chatModel = getChatModel(model);

		// when
		String answer = chatModel.chat("Provide 3 short bullet points explaining why Java is awesome");

		// then
		assertNotNull(answer);
	}

	@Tag("integration")
	@ParameterizedTest()
	@ValueSource(strings = { NON_THINKING_MODEL, THINKING_MODEL })
	void ollamaAxcelEvaluation(String model)
	{
		// given
		AxcelEvaluator evaluator = new AxcelEvaluator(getChatModel(model));
		Dialog dialog = DIALOG_LOADER.getRawDialog();
		AxcelDataLoader loader = new AxcelDataLoader();

		// when
		Evaluation evaluation = evaluator.evaluate(dialog.getSample(), loader.loadExampleData());

		// then
		assertNotNull(evaluation);
		System.out.printf("Evaluation result: %s = %.2f%n\n%s", evaluation.getName(), evaluation.getValue(), evaluation.getExplanations());
	}

	private static OllamaChatModel getChatModel(String model)
	{
		return OllamaChatModel.builder()
			.baseUrl("http://localhost:11434")
			.supportedCapabilities(RESPONSE_FORMAT_JSON_SCHEMA)
			.modelName(model)
			.build();
	}
}
