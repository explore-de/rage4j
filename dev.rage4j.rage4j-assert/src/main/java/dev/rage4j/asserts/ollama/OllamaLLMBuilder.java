package dev.rage4j.asserts.ollama;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.rage4j.asserts.LLMBuilder;
import dev.rage4j.asserts.RageAssert;

/**
 * Builder for creating RageAssert instances configured for Ollama models.
 * Supports both standalone model creation and injection of framework-managed
 * models.
 */
public class OllamaLLMBuilder implements LLMBuilder<OllamaLLMBuilder>
{
	private static final String DEFAULT_CHAT_MODEL = "llama3.2";
	private static final String DEFAULT_EMBEDDING_MODEL = "nomic-embed-text";
	private static final String DEFAULT_BASE_URL = "http://localhost:11434";

	private String chatModelName = DEFAULT_CHAT_MODEL;
	private String embeddingModelName = DEFAULT_EMBEDDING_MODEL;
	private String baseUrl = DEFAULT_BASE_URL;

	// Support for injecting framework-managed models (e.g., from Quarkus)
	private ChatModel customChatModel;
	private EmbeddingModel customEmbeddingModel;

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
	 *            The base URL (e.g., "http://localhost:11434")
	 * @return This builder instance for method chaining
	 */
	public OllamaLLMBuilder withBaseUrl(String baseUrl)
	{
		this.baseUrl = baseUrl;
		return this;
	}

	/**
	 * Injects a custom ChatModel instance (e.g., from a DI framework like
	 * Quarkus). When set, standalone model creation is bypassed.
	 *
	 * @param chatModel
	 *            The ChatModel instance to use
	 * @return This builder instance for method chaining
	 */
	public OllamaLLMBuilder withChatLanguageModel(ChatModel chatModel)
	{
		this.customChatModel = chatModel;
		return this;
	}

	/**
	 * Injects a custom EmbeddingModel instance (e.g., from a DI framework like
	 * Quarkus). When set, standalone model creation is bypassed.
	 *
	 * @param embeddingModel
	 *            The EmbeddingModel instance to use
	 * @return This builder instance for method chaining
	 */
	public OllamaLLMBuilder withCustomEmbeddingModel(EmbeddingModel embeddingModel)
	{
		this.customEmbeddingModel = embeddingModel;
		return this;
	}

	/**
	 * Creates a RageAssert instance. For Ollama, no API key is required.
	 *
	 * @param apiKey
	 *            Ignored for Ollama (kept for interface compatibility)
	 * @return A configured RageAssert instance
	 */
	@Override
	public RageAssert fromApiKey(String apiKey)
	{
		return build();
	}

	/**
	 * Creates a RageAssert instance with the configured models. Uses custom
	 * models if provided, otherwise creates standalone Ollama models.
	 *
	 * @return A configured RageAssert instance
	 */
	public RageAssert build()
	{
		ChatModel chatModel = customChatModel != null
			? customChatModel
			: OllamaChatModel.builder()
				.baseUrl(this.baseUrl)
				.modelName(this.chatModelName)
				.build();

		EmbeddingModel embeddingModel = customEmbeddingModel != null
			? customEmbeddingModel
			: OllamaEmbeddingModel.builder()
				.baseUrl(this.baseUrl)
				.modelName(this.embeddingModelName)
				.build();

		return new RageAssert(chatModel, embeddingModel);
	}
}