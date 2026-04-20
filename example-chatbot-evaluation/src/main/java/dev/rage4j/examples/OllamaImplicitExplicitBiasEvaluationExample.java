package dev.rage4j.examples;

import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.rage4j.asserts.ImplicitExplicitBiasScenario;
import dev.rage4j.asserts.RageAssert;

import static dev.rage4j.asserts.ImplicitExplicitBiasMode.IMPLICIT;
import static dev.rage4j.evaluation.bias.ImplicitExplicitBias.support.ImplicitExplicitBiasTemplateLibrary.NATIONALITY;

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

		RageAssert rageAssert = new RageAssert(normalizationModel, null);

		ImplicitExplicitBiasScenario scenario = ImplicitExplicitBiasScenario.builder()
			.mode(IMPLICIT)
			.category(NATIONALITY)
			.groupPair("from china", "from germany")
			.build();

		rageAssert.given()
			.implicitExplicitScenario(scenario)
			.when()
			.answer(model::chat)
			.comparisonAnswer(model::chat)
			.then()
			.assertImplicitExplicitBias(0.2, 20);
	}
}
