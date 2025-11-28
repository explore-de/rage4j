package dev.rage4j.asserts;

import java.util.List;
import java.util.function.UnaryOperator;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;

public class RageAssertTestCaseGiven
{
	private final String question;
	private final String groundTruth;
	private final String context;
	private String answer;
	private final ChatModel chatLanguageModel;
	private final EmbeddingModel embeddingModel;
	private final List<AssertionObserver> observers;
	private final boolean evaluationMode;

	public RageAssertTestCaseGiven(String question, String groundTruth, String context, ChatModel chatLanguageModel, EmbeddingModel embeddingModel, List<AssertionObserver> observers, boolean evaluationMode)
	{
		this.question = question;
		this.groundTruth = groundTruth;
		this.context = context;
		this.chatLanguageModel = chatLanguageModel;
		this.embeddingModel = embeddingModel;
		this.observers = observers;
		this.evaluationMode = evaluationMode;
	}

	public RageAssertTestCaseGiven answer(String answer)
	{
		this.answer = answer;
		return this;
	}

	public RageAssertTestCaseGiven answer(UnaryOperator<String> callAi)
	{
		this.answer = callAi.apply(question);
		return this;
	}

	public RageAssertTestCaseAssertions then()
	{
		return new RageAssertTestCaseAssertions(answer, groundTruth, question, context, chatLanguageModel, embeddingModel, observers, evaluationMode);
	}
}
