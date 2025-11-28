package dev.rage4j.asserts.openai;

import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.rage4j.asserts.LLMBuilder;
import dev.rage4j.asserts.RageAssert;

public class OpenAiLLMBuilder implements LLMBuilder<OpenAiLLMBuilder>
{
	private static final String DEFAULT_CHAT_MODEL = "gpt-5.1";
	private static final String DEFAULT_EMBEDDING_MODEL = "text-embedding-3-small";

	private String chatModelName = DEFAULT_CHAT_MODEL;
	private String embeddingModelName = DEFAULT_EMBEDDING_MODEL;

	@Override
	public OpenAiLLMBuilder withChatModel(String modelName)
	{
		this.chatModelName = modelName;
		return this;
	}

	@Override
	public OpenAiLLMBuilder withEmbeddingModel(String modelName)
	{
		this.embeddingModelName = modelName;
		return this;
	}

	@Override
	public RageAssert fromApiKey(String apiKey)
	{
		OpenAiChatModel chatModel = OpenAiChatModel.builder()
			.apiKey(apiKey)
			.modelName(chatModelName)
			.build();

		OpenAiEmbeddingModel embeddingModel = OpenAiEmbeddingModel.builder()
			.apiKey(apiKey)
			.modelName(embeddingModelName)
			.build();

		return new RageAssert(chatModel, embeddingModel);
	}
}
