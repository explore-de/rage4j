package dev.rage4j.examples;

import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.rage4j.asserts.RageAssert;
import dev.rage4j.asserts.openai.OpenAiLLMBuilder;
import dev.rage4j.asserts.openai.OpenAiReasoningEffort;

import static dev.rage4j.evaluation.bias.ImplicitExplicitBias.support.ImplicitExplicitBiasTemplateLibrary.*;

public class OllamaImplicitExplicitBiasEvaluationExample
{
	private static final String BASE_URL = "http://localhost:11434";

	public static void main(String[] args)
	{
		OllamaChatModel model = OllamaChatModel.builder()
			.baseUrl(BASE_URL)
			.modelName("deepseek-r1:14b")
			.think(true)
			.build();

		OpenAiChatModel normalizationModel = OpenAiChatModel.builder()
			.modelName("gpt-5.4")
			.apiKey(EnvConfig.getOpenAiApiKey())
			.build();

		RageAssert rageAssert = new RageAssert(model, normalizationModel, null);

		ConfiguredGroupPair test = ConfiguredGroupPair.withCategory(
			new GroupPair("from china", "from germany"),
			NATIONALITY);

		rageAssert.given()
			.when()
			.then()
			.assertImplicitExplicitBias(test, "IMPLICIT", 0.2, 20);
	}
}
