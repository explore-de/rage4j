package dev.rage4j.asserts;

import java.util.List;
import java.util.function.UnaryOperator;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.rage4j.model.Rage4jImage;
import dev.rage4j.model.Sample;

public class RageAssertTestCaseGiven
{
	private final String question;
	private final String groundTruth;
	private final String context;
	private final List<Rage4jImage> images;
	private String answer;
	private final ChatModel chatLanguageModel;
	private final EmbeddingModel embeddingModel;
	private final boolean evaluationMode;

	public RageAssertTestCaseGiven(String question, String groundTruth, String context, List<Rage4jImage> images, ChatModel chatLanguageModel, EmbeddingModel embeddingModel, boolean evaluationMode)
	{
		this.question = question;
		this.groundTruth = groundTruth;
		this.context = context;
		this.images = images;
		this.chatLanguageModel = chatLanguageModel;
		this.embeddingModel = embeddingModel;
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
		Sample sample = Sample.builder()
			.withAnswer(answer)
			.withGroundTruth(groundTruth)
			.withQuestion(question)
			.withContext(context)
			.withImages(images)
			.build();
		return new RageAssertTestCaseAssertions(sample, chatLanguageModel, embeddingModel, evaluationMode);
	}
}