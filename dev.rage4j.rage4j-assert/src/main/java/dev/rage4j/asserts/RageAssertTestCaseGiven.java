package dev.rage4j.asserts;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;

import java.util.List;
import java.util.function.Function;

public class RageAssertTestCaseGiven
{
	private String question;
	private String groundTruth;
	private List<String> contextList;
	private String answer;
	private ChatModel chatLanguageModel;
	private EmbeddingModel embeddingModel;

	public RageAssertTestCaseGiven(String question, String groundTruth, List<String> contextList, ChatModel chatLanguageModel, EmbeddingModel embeddingModel)
	{
		this.question = question;
		this.groundTruth = groundTruth;
		this.contextList = contextList;
		this.chatLanguageModel = chatLanguageModel;
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

	public RageAssertTestCaseAssertions then()
	{
		return new RageAssertTestCaseAssertions(answer, groundTruth, question, contextList, chatLanguageModel, embeddingModel);
	}
}
