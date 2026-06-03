package dev.rage4j.asserts.ollama;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.rage4j.asserts.RageAssert;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class OllamaLLMBuilderTest
{
	@Test
	void testBuilderCreatesRageAssert()
	{
		RageAssert rageAssert = new OllamaLLMBuilder().build();
		assertNotNull(rageAssert);
	}

	@Test
	void testBuilderWithCustomBaseUrl()
	{
		RageAssert rageAssert = new OllamaLLMBuilder().withBaseUrl("http://custom:11434").build();
		assertNotNull(rageAssert);
	}

	@Test
	void testBuilderWithCustomChatModel()
	{
		RageAssert rageAssert = new OllamaLLMBuilder().withChatModel("llama3").build();
		assertNotNull(rageAssert);
	}

	@Test
	void testBuilderWithCustomEmbeddingModel()
	{
		RageAssert rageAssert = new OllamaLLMBuilder().withEmbeddingModel("all-minilm").build();
		assertNotNull(rageAssert);
	}

	@Test
	void testBuilderWithAllCustomizations()
	{
		RageAssert rageAssert = new OllamaLLMBuilder().withBaseUrl("http://custom:11434")
			.withChatModel("llama3")
			.withEmbeddingModel("all-minilm")
			.build();
		assertNotNull(rageAssert);
	}

	@Test
	void testFromApiKeyIgnoresApiKey()
	{
		// Ollama doesn't need an API key, but we implement the interface
		RageAssert rageAssert = new OllamaLLMBuilder().fromApiKey("ignored");
		assertNotNull(rageAssert);
	}

	@Test
	void testMethodChaining()
	{
		RageAssert rageAssert = new OllamaLLMBuilder().withChatModel("llama3")
			.withEmbeddingModel("all-minilm")
			.withBaseUrl("http://localhost:11434")
			.build()
			.withEvaluationMode();

		assertNotNull(rageAssert);
	}

	@Test
	void testWithChatLanguageModelInjection()
	{
		// Use a real OllamaChatModel instance for testing injection
		ChatModel chatModel = OllamaChatModel.builder()
			.baseUrl("http://localhost:11434")
			.modelName("test-model")
			.build();

		RageAssert rageAssert = new OllamaLLMBuilder()
			.withChatLanguageModel(chatModel)
			.build();

		assertNotNull(rageAssert);
	}

	@Test
	void testWithCustomEmbeddingModelInjection()
	{
		// Use a real OllamaEmbeddingModel instance for testing injection
		EmbeddingModel embeddingModel = OllamaEmbeddingModel.builder()
			.baseUrl("http://localhost:11434")
			.modelName("test-embedding")
			.build();

		RageAssert rageAssert = new OllamaLLMBuilder()
			.withCustomEmbeddingModel(embeddingModel)
			.build();

		assertNotNull(rageAssert);
	}

	@Test
	void testWithBothCustomModelsInjection()
	{
		// Use real Ollama model instances for testing injection
		ChatModel chatModel = OllamaChatModel.builder()
			.baseUrl("http://localhost:11434")
			.modelName("test-model")
			.build();

		EmbeddingModel embeddingModel = OllamaEmbeddingModel.builder()
			.baseUrl("http://localhost:11434")
			.modelName("test-embedding")
			.build();

		RageAssert rageAssert = new OllamaLLMBuilder()
			.withChatLanguageModel(chatModel)
			.withCustomEmbeddingModel(embeddingModel)
			.build();

		assertNotNull(rageAssert);
	}
}
