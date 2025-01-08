package dev.rage4j.core;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;

public class RageAssert
{
	private final ChatLanguageModel chatLanguageModel;
	private final EmbeddingModel embeddingModel;

	public RageAssert(ChatLanguageModel chatLanguageModel, EmbeddingModel embeddingModel)
	{
		this.chatLanguageModel = chatLanguageModel;
		this.embeddingModel = embeddingModel;
	}

	public RageAssertTestCaseBuilder given()
	{
		return new RageAssertTestCaseBuilder(chatLanguageModel, embeddingModel);
	}
}