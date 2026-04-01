package dev.rage4j.asserts;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.rage4j.model.Rage4jImage;
import dev.rage4j.model.Sample;

import java.util.List;
import java.util.function.Function;

public class RageAssertTestCaseGiven
{
	private final String question;
	private final String groundTruth;
	private final String context;
	private final List<Rage4jImage> images;
	private final String comparisonQuestion;
	private final String comparisonGroundTruth;
	private final String comparisonContext;
	private final ImplicitExplicitScenario implicitExplicitScenario;
	private String answer;
	private String comparisonAnswer;
	private final ChatModel judgeChatModel;
	private final ChatModel evaluatedChatModel;
	private final EmbeddingModel embeddingModel;
	private final boolean evaluationMode;

	public RageAssertTestCaseGiven(
		String question,
		String groundTruth,
		String context,
		List<Rage4jImage> images,
		String comparisonQuestion,
		String comparisonGroundTruth,
		String comparisonContext,
		ImplicitExplicitScenario implicitExplicitScenario,
		ChatModel judgeChatModel,
		ChatModel evaluatedChatModel,
		EmbeddingModel embeddingModel,
		boolean evaluationMode)
	{
		this.question = question;
		this.groundTruth = groundTruth;
		this.context = context;
		this.images = images;
		this.comparisonQuestion = comparisonQuestion;
		this.comparisonGroundTruth = comparisonGroundTruth;
		this.comparisonContext = comparisonContext;
		this.implicitExplicitScenario = implicitExplicitScenario;
		this.judgeChatModel = judgeChatModel;
		this.evaluatedChatModel = evaluatedChatModel;
		this.embeddingModel = embeddingModel;
		this.evaluationMode = evaluationMode;
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
		Sample comparisonSample = buildComparisonSample();
		Sample.SampleBuilder builder = Sample.builder()
			.withAnswer(answer)
			.withGroundTruth(groundTruth)
			.withQuestion(question)
			.withContext(context)
			.withImages(images);

		if (comparisonSample != null)
		{
			builder.withComparisonSample(comparisonSample);
		}

		return new RageAssertTestCaseAssertions(
			builder.build(),
			implicitExplicitScenario,
			judgeChatModel,
			evaluatedChatModel,
			embeddingModel,
			evaluationMode);
	}

	private Sample buildComparisonSample()
	{
		if (comparisonQuestion == null && comparisonAnswer == null && comparisonGroundTruth == null && comparisonContext == null)
		{
			return null;
		}

		return Sample.builder()
			.withAnswer(comparisonAnswer)
			.withGroundTruth(comparisonGroundTruth)
			.withQuestion(comparisonQuestion)
			.withContext(comparisonContext)
			.build();
	}
}
