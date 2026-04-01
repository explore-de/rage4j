package dev.rage4j.asserts;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;

import java.util.List;

public class RageAssertTestCaseBuilder
{
	private String question;
	private String groundTruth;
	private List<String> contextList;
	private String comparisonQuestion;
	private String comparisonGroundTruth;
	private List<String> comparisonContextList;
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

	public RageAssertTestCaseBuilder comparisonQuestion(String comparisonQuestion)
	{
		this.comparisonQuestion = comparisonQuestion;
		return this;
	}

	public RageAssertTestCaseBuilder comparisonGroundTruth(String comparisonGroundTruth)
	{
		this.comparisonGroundTruth = comparisonGroundTruth;
		return this;
	}

	public RageAssertTestCaseBuilder comparisonContextList(List<String> comparisonContextList)
	{
		this.comparisonContextList = comparisonContextList;
		return this;
	}

	public RageAssertTestCaseGiven when()
	{
		return new RageAssertTestCaseGiven(
			question,
			groundTruth,
			contextList,
			comparisonQuestion,
			comparisonGroundTruth,
			comparisonContextList,
			chatModel,
			embeddingModel);
	}
}
