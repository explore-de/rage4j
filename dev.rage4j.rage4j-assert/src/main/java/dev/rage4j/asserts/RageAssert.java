package dev.rage4j.asserts;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;

public class RageAssert
{
	private final ChatModel chatModel;
	private final ChatModel judgeChatModel;
	private final EmbeddingModel embeddingModel;

	public RageAssert(ChatModel chatLanguageModel, EmbeddingModel embeddingModel)
	{
		this(chatLanguageModel, null, embeddingModel);
	}

	public RageAssert(ChatModel chatLanguageModel, ChatModel judgeChatModel, EmbeddingModel embeddingModel)
	{
		this.chatModel = chatLanguageModel;
		this.judgeChatModel = judgeChatModel;
		this.embeddingModel = embeddingModel;
	}

	public RageAssertTestCaseBuilder given()
	{
		return new RageAssertTestCaseBuilder(chatModel, judgeChatModel, embeddingModel);
	}
}
