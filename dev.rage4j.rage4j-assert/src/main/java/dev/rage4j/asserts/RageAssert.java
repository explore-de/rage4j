package dev.rage4j.asserts;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;

public class RageAssert
{
	private final ChatModel judgeChatModel;
	private final EmbeddingModel embeddingModel;

	public RageAssert(ChatModel judgeChatModel, EmbeddingModel embeddingModel)
	{
		this.judgeChatModel = judgeChatModel;
		this.embeddingModel = embeddingModel;
	}

	public RageAssert(ChatModel judgeChatModel) {
		this(judgeChatModel, null);
	}

	public RageAssertTestCaseBuilder given()
	{
		return new RageAssertTestCaseBuilder(judgeChatModel, embeddingModel);
	}
}
