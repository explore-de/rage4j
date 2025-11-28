package dev.rage4j.asserts;

public interface LLMBuilder<T extends LLMBuilder<T>>
{
	/**
	 * Sets the chat model to use for evaluations.
	 *
	 * @param modelName
	 *            The name of the chat model.
	 * @return This builder instance for method chaining.
	 */
	T withChatModel(String modelName);

	/**
	 * Sets the embedding model to use for semantic similarity evaluations.
	 *
	 * @param modelName
	 *            The name of the embedding model.
	 * @return This builder instance for method chaining.
	 */
	T withEmbeddingModel(String modelName);

	/**
	 * Creates a RageAssert instance using the provided API key.
	 *
	 * @param apiKey
	 *            The API key for the LLM provider.
	 * @return A configured RageAssert instance.
	 */
	RageAssert fromApiKey(String apiKey);
}
