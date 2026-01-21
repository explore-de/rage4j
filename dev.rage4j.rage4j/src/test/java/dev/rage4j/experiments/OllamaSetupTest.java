package dev.rage4j.experiments;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.rage4j.evaluation.Evaluation;
import dev.rage4j.evaluation.axcel.AxcelDataLoader;
import dev.rage4j.evaluation.axcel.AxcelEvaluator;
import dev.rage4j.experiments.enity.Dialog;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static dev.langchain4j.model.chat.Capability.RESPONSE_FORMAT_JSON_SCHEMA;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class OllamaSetupTest
{
	private static final ChatModel CHAT_MODEL = getChatModel();
	private static final DialogLoader DIALOG_LOADER = new DialogLoader();
	private static final String MODEL_NAME = "ministral-3:3b";

	@Test
	@Tag("integration")
	void ollamaEvaluation()
	{
		String answer = CHAT_MODEL.chat("Provide 3 short bullet points explaining why Java is awesome");

		assertNotNull(answer);
	}

	@Test
	@Tag("integration")
	void ollamaAxcelEvaluation()
	{
		// given
		AxcelEvaluator evaluator = new AxcelEvaluator(CHAT_MODEL);
		Dialog dialog = DIALOG_LOADER.getRawDialog();
		AxcelDataLoader loader = new AxcelDataLoader();

		// when
		Evaluation evaluation = evaluator.evaluate(dialog.getSample(), loader.loadExampleData());

		// then
		assertNotNull(evaluation);
		System.out.printf("Evaluation result: %s = %.2f%n\n%s", evaluation.getName(), evaluation.getValue(), evaluation.getExplanations());
	}

	private static OllamaChatModel getChatModel()
	{
		return OllamaChatModel.builder()
			.baseUrl("http://localhost:11434")
			.supportedCapabilities(RESPONSE_FORMAT_JSON_SCHEMA)
			.modelName(MODEL_NAME)
			.build();
	}
}
