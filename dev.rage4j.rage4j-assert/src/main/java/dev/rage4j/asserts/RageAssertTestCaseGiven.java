package dev.rage4j.asserts;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;

import java.util.function.Function;

public class RageAssertTestCaseGiven
{
	private String question;
	private String groundTruth;
	private String context;
	private String comparisonQuestion;
	private String comparisonGroundTruth;
	private String comparisonContext;
	private ImplicitExplicitScenario implicitExplicitScenario;
	private String answer;
	private String comparisonAnswer;
	private ChatModel judgeChatModel;
	private ChatModel evaluatedChatModel;
	private EmbeddingModel embeddingModel;

	public RageAssertTestCaseGiven(String question, String groundTruth, String context, String comparisonQuestion, String comparisonGroundTruth, String comparisonContext, ImplicitExplicitScenario implicitExplicitScenario, ChatModel judgeChatModel, ChatModel evaluatedChatModel, EmbeddingModel embeddingModel)
	{
		this.question = question;
		this.groundTruth = groundTruth;
		this.context = context;
		this.comparisonQuestion = comparisonQuestion;
		this.comparisonGroundTruth = comparisonGroundTruth;
		this.comparisonContext = comparisonContext;
		this.implicitExplicitScenario = implicitExplicitScenario;
		this.judgeChatModel = judgeChatModel;
		this.evaluatedChatModel = evaluatedChatModel;
		this.embeddingModel = embeddingModel;
	}

	public RageAssertTestCaseGiven answer(String answer)
	{
		this.answer = answer;
		return this;
	}

	public RageAssertTestCaseGiven answer(Function<String, String> callAi)
	{
		this.answer = callAi.apply(question);
		return this;
	}

	public RageAssertTestCaseGiven comparisonAnswer(String comparisonAnswer)
	{
		this.comparisonAnswer = comparisonAnswer;
		return this;
	}

	public RageAssertTestCaseGiven comparisonAnswer(Function<String, String> callAi)
	{
		if (comparisonQuestion == null || comparisonQuestion.trim().isEmpty())
		{
			throw new IllegalStateException("comparisonQuestion must be set before comparisonAnswer(Function) is used");
		}
		this.comparisonAnswer = callAi.apply(comparisonQuestion);
		return this;
	}

	public RageAssertTestCaseAssertions then()
	{
		return new RageAssertTestCaseAssertions(answer, groundTruth, question, context, comparisonAnswer, comparisonGroundTruth, comparisonQuestion, comparisonContext, implicitExplicitScenario, judgeChatModel, evaluatedChatModel, embeddingModel);
	}
}
