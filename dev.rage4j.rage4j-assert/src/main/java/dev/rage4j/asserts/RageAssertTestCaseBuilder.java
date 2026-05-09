package dev.rage4j.asserts;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;

public class RageAssertTestCaseBuilder
{
	private String question;
	private String groundTruth;
	private String context;
	private String comparisonQuestion;
	private String comparisonGroundTruth;
	private String comparisonContext;
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

	public RageAssertTestCaseBuilder context(String context)
	{
		this.context = context;
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

	public RageAssertTestCaseBuilder comparisonContext(String comparisonContext)
	{
		this.comparisonContext = comparisonContext;
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
		this.context = scenario.qualifications();
		this.comparisonContext = scenario.qualifications();
		return this;
	}

	public RageAssertTestCaseGiven when()
	{
		return new RageAssertTestCaseGiven(
			question,
			groundTruth,
			context,
			comparisonQuestion,
			comparisonGroundTruth,
			comparisonContext,
			implicitExplicitScenario,
			judgeChatModel,
			evaluatedChatModel,
			embeddingModel);
	}
}
