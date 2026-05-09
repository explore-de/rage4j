package dev.rage4j.asserts;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;

public class RageAssert
{
	private final ChatModel judgeChatModel;
	private final ChatModel evaluatedChatModel;
	private final EmbeddingModel embeddingModel;

	public RageAssert(ChatModel judgeChatModel, EmbeddingModel embeddingModel)
	{
		this(judgeChatModel, judgeChatModel, embeddingModel);
	}

	public RageAssert(ChatModel judgeChatModel, ChatModel evaluatedChatModel)
	{
		this(judgeChatModel, evaluatedChatModel, null);
	}

	public RageAssert(ChatModel judgeChatModel, ChatModel evaluatedChatModel, EmbeddingModel embeddingModel)
	{
		this.judgeChatModel = judgeChatModel;
		this.evaluatedChatModel = evaluatedChatModel;
		this.embeddingModel = embeddingModel;
	}

	public RageAssert(ChatModel judgeChatModel) {
		this(judgeChatModel, (EmbeddingModel) null);
	}

	public RageAssertTestCaseBuilder given()
	{
		return new RageAssertTestCaseBuilder(judgeChatModel, evaluatedChatModel, embeddingModel);
	}
}
