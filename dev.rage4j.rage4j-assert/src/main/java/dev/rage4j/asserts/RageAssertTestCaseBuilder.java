package dev.rage4j.asserts;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;

import java.util.List;

public class RageAssertTestCaseBuilder
{
	private String question;
	private String groundTruth;
	private List<String> contextList;
	private ChatModel chatModel;
	private final EmbeddingModel embeddingModel;

	public RageAssertTestCaseBuilder(ChatModel chatModel, EmbeddingModel embeddingModel)
	{
		this.chatModel = chatModel;
		this.embeddingModel = embeddingModel;
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

	public RageAssertTestCaseBuilder contextList(List<String> contextList)
	{
		this.contextList = contextList;
		return this;
	}

	public RageAssertTestCaseGiven when()
	{
		return new RageAssertTestCaseGiven(question, groundTruth, contextList, chatModel, embeddingModel);
	}
}
