package dev.rage4j.asserts.ollama;

import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.rage4j.asserts.LLMBuilder;
import dev.rage4j.asserts.RageAssert;

public class OllamaLLMBuilder implements LLMBuilder<OllamaLLMBuilder>
{
	private static final String DEFAULT_CHAT_MODEL = "llama3.2";
	private static final String DEFAULT_EMBEDDING_MODEL = "nomic-embed-text";
	private static final String DEFAULT_BASE_URL = "http://localhost:11434";

	private String chatModelName = DEFAULT_CHAT_MODEL;
	private String embeddingModelName = DEFAULT_EMBEDDING_MODEL;
	private String baseUrl = DEFAULT_BASE_URL;

	@Override
	public OllamaLLMBuilder withChatModel(String modelName)
	{
		this.chatModelName = modelName;
		return this;
	}

	@Override
	public OllamaLLMBuilder withEmbeddingModel(String modelName)
	{
		this.embeddingModelName = modelName;
		return this;
	}

	/**
	 * Sets the base URL for the Ollama server.
	 *
	 * @param baseUrl
	 *            The base URL of the Ollama server (e.g.,
	 *            "http://localhost:11434").
	 * @return This builder instance for method chaining.
	 */
	public OllamaLLMBuilder withBaseUrl(String baseUrl)
	{
		this.baseUrl = baseUrl;
		return this;
	}

	@Override
	public RageAssert fromApiKey(String apiKey)
	{
		// Ollama doesn't require an API key, but we need to implement the
		// interface
		// The apiKey parameter is ignored for Ollama
		return build();
	}

	/**
	 * Creates a RageAssert instance. Ollama doesn't require an API key, so this
	 * method is provided for convenience.
	 *
	 * @return A configured RageAssert instance.
	 */
	public RageAssert build()
	{
		OllamaChatModel chatModel = OllamaChatModel.builder()
			.baseUrl(baseUrl)
			.modelName(chatModelName)
			.build();

		OllamaEmbeddingModel embeddingModel = OllamaEmbeddingModel.builder()
			.baseUrl(baseUrl)
			.modelName(embeddingModelName)
			.build();

		return new RageAssert(chatModel, embeddingModel);
	}
}
