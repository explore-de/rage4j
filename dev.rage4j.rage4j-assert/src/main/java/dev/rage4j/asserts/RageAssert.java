package dev.rage4j.asserts;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;

public class RageAssert
{
	private final ChatModel chatModel;
	private final ChatModel judgeChatModel;
	private final EmbeddingModel embeddingModel;
	private boolean evaluationMode = false;

	public RageAssert(ChatModel chatLanguageModel)
	{
		this(chatLanguageModel, (EmbeddingModel)null);
	}

	public RageAssert(ChatModel chatLanguageModel, EmbeddingModel embeddingModel)
	{
		this(chatLanguageModel, chatLanguageModel, embeddingModel);
	}

	public RageAssert(ChatModel chatLanguageModel, ChatModel judgeChatModel)
	{
		this(chatLanguageModel, judgeChatModel, null);
	}

	public RageAssert(ChatModel chatLanguageModel, ChatModel judgeChatModel, EmbeddingModel embeddingModel)
	{
		this.chatModel = chatLanguageModel;
		this.judgeChatModel = judgeChatModel;
		this.embeddingModel = embeddingModel;
	}

	public RageAssert withEvaluationMode()
	{
		this.evaluationMode = true;
		return this;
	}

	public RageAssert withStrictMode()
	{
		this.evaluationMode = false;
		return this;
	}

	boolean isEvaluationMode()
	{
		return evaluationMode;
	}

	public RageAssertTestCaseBuilder given()
	{
		return new RageAssertTestCaseBuilder(judgeChatModel, chatModel, embeddingModel, evaluationMode);
	}
}
