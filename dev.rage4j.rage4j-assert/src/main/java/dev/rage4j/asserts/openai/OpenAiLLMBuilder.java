package dev.rage4j.asserts.openai;

import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.rage4j.asserts.LLMBuilder;
import dev.rage4j.asserts.RageAssert;

public class OpenAiLLMBuilder implements LLMBuilder
{
	private static final String DEFAULT_CHAT_MODEL = "gpt-4";
	private static final String DEFAULT_EMBEDDING_MODEL = "text-embedding-3-small";

	@Override
	public RageAssert fromApiKey(String apiKey)
	{
		OpenAiChatModel chatModel = OpenAiChatModel.builder()
			.apiKey(apiKey)
			.modelName(DEFAULT_CHAT_MODEL)
			.build();

		OpenAiEmbeddingModel embeddingModel = OpenAiEmbeddingModel.builder()
			.apiKey(apiKey)
			.modelName(DEFAULT_EMBEDDING_MODEL)
			.build();

		return new RageAssert(chatModel, embeddingModel);
	}
}