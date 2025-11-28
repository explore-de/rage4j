package dev.rage4j.asserts;

import java.util.List;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;

public class RageAssertTestCaseBuilder
{
	private String question;
	private String groundTruth;
	private String context;
	private final ChatModel chatModel;
	private final EmbeddingModel embeddingModel;
	private final List<AssertionObserver> observers;
	private final boolean evaluationMode;

	public RageAssertTestCaseBuilder(ChatModel chatModel, EmbeddingModel embeddingModel, List<AssertionObserver> observers, boolean evaluationMode)
	{
		this.chatModel = chatModel;
		this.embeddingModel = embeddingModel;
		this.observers = observers;
		this.evaluationMode = evaluationMode;
	}

	public RageAssertTestCaseBuilder question(String question)
	{
		this.question = question;
		return this;
	}

	public RageAssertTestCaseBuilder groundTruth(String groundTruth)
	{
		this.groundTruth = groundTruth;
		return this;
	}

	public RageAssertTestCaseBuilder context(String context)
	{
		this.context = context;
		return this;
	}

	public RageAssertTestCaseGiven when()
	{
		return new RageAssertTestCaseGiven(question, groundTruth, context, chatModel, embeddingModel, observers, evaluationMode);
	}
}
