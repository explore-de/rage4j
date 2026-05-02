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
	private ImplicitExplicitScenario implicitExplicitScenario;
	private final ChatModel judgeChatModel;
	private final ChatModel evaluatedChatModel;
	private final EmbeddingModel embeddingModel;

	public RageAssertTestCaseBuilder(ChatModel judgeChatModel, EmbeddingModel embeddingModel)
	{
		this(judgeChatModel, null, embeddingModel);
	}

	public RageAssertTestCaseBuilder(ChatModel judgeChatModel, ChatModel evaluatedChatModel, EmbeddingModel embeddingModel)
	{
		this.judgeChatModel = judgeChatModel;
		this.evaluatedChatModel = evaluatedChatModel;
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

	public RageAssertTestCaseBuilder context(String context)
	{
		this.contextList = context == null ? null : List.of(context);
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

	public RageAssertTestCaseBuilder comparisonContext(String comparisonContext)
	{
		this.comparisonContextList = comparisonContext == null ? null : List.of(comparisonContext);
		return this;
	}

	public RageAssertTestCaseBuilder implicitExplicitScenario(ImplicitExplicitScenario scenario)
	{
		this.implicitExplicitScenario = scenario;
		if (scenario == null)
		{
			return this;
		}

		this.question = scenario.question();
		this.comparisonQuestion = scenario.comparisonQuestion();
		this.contextList = List.of(scenario.qualifications());
		this.comparisonContextList = List.of(scenario.qualifications());
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
				implicitExplicitScenario,
			judgeChatModel,
			evaluatedChatModel,
			embeddingModel);
	}
}
