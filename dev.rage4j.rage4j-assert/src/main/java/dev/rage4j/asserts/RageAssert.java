package dev.rage4j.asserts;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;

public class RageAssert
{
	private final ChatModel chatModel;
	private final EmbeddingModel embeddingModel;

	public RageAssert(ChatModel chatLanguageModel, EmbeddingModel embeddingModel)
	{
		this.chatModel = chatLanguageModel;
		this.embeddingModel = embeddingModel;
	}

	public RageAssertTestCaseBuilder given()
	{
		return new RageAssertTestCaseBuilder(chatModel, embeddingModel);
	}
}