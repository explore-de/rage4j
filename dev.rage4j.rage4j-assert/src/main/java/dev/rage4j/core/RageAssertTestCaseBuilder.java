package dev.rage4j.core;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;

import java.util.List;

public class RageAssertTestCaseBuilder
{
	private String question;
	private String groundTruth;
	private List<String> contextList;
	private ChatLanguageModel chatLanguageModel;
	private final EmbeddingModel embeddingModel;

	public RageAssertTestCaseBuilder(ChatLanguageModel chatLanguageModel, EmbeddingModel embeddingModel)
	{
		this.chatLanguageModel = chatLanguageModel;
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
		return new RageAssertTestCaseGiven(question, groundTruth, contextList, chatLanguageModel, embeddingModel);
	}
}
