package dev.rage4j.asserts;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.rage4j.asserts.exception.Rage4JCorrectnessException;
import dev.rage4j.asserts.exception.Rage4JFaithfulnessException;
import dev.rage4j.asserts.exception.Rage4JRelevanceException;
import dev.rage4j.asserts.exception.Rage4JSimilarityException;
import dev.rage4j.evaluation.Evaluation;
import dev.rage4j.evaluation.answercorrectness.AnswerCorrectnessEvaluator;
import dev.rage4j.evaluation.answerrelevance.AnswerRelevanceEvaluator;
import dev.rage4j.evaluation.answersemanticsimilarity.AnswerSemanticSimilarityEvaluator;
import dev.rage4j.evaluation.faithfulness.FaithfulnessEvaluator;
import dev.rage4j.model.Sample;

import java.util.List;

public class RageAssertTestCaseAssertions
{
	private ChatLanguageModel chatLanguageModel;
	private EmbeddingModel embeddingModel;
	private String question;
	private String groundTruth;
	private List<String> contextList;
	private String answer;

	public RageAssertTestCaseAssertions(String answer, String groundTruth, String question, List<String> contextList, ChatLanguageModel chatLanguageModel, EmbeddingModel embeddingModel)
	{
		this.answer = answer;
		this.groundTruth = groundTruth;
		this.question = question;
		this.contextList = contextList;
		this.chatLanguageModel = chatLanguageModel;
		this.embeddingModel = embeddingModel;
	}

	public Evaluation assertFaithfulness(double minValue)
	{
		FaithfulnessEvaluator evaluator = new FaithfulnessEvaluator(chatLanguageModel);
		Sample sample = Sample.builder()
			.withAnswer(answer)
			.withGroundTruth(groundTruth)
			.withQuestion(question)
			.withContextsList(contextList)
			.build();
		Evaluation evaluation = evaluator.evaluate(sample);

		if (minValue > evaluation.getValue())
		{
			throw new Rage4JFaithfulnessException("Answer did not reach required min value! Evaluated value: " + evaluation.getValue() + " answer: " + answer);
		}
		return evaluation;
	}

	public Evaluation assertAnswerCorrectness(double minValue)
	{
		AnswerCorrectnessEvaluator evaluator = new AnswerCorrectnessEvaluator(chatLanguageModel);
		Sample sample = Sample.builder()
			.withAnswer(answer)
			.withGroundTruth(groundTruth)
			.withQuestion(question)
			.build();
		Evaluation evaluation = evaluator.evaluate(sample);

		if (minValue > evaluation.getValue())
		{
			throw new Rage4JCorrectnessException("Answer did not reach required min value! Evaluated value: " + evaluation.getValue() + " answer: " + answer);
		}
		return evaluation;
	}

	public Evaluation assertAnswerRelevance(double minValue)
	{
		AnswerRelevanceEvaluator evaluator = new AnswerRelevanceEvaluator(chatLanguageModel, embeddingModel);
		Sample sample = Sample.builder()
			.withAnswer(answer)
			.withQuestion(question)
			.withContextsList(contextList)
			.build();
		Evaluation evaluation = evaluator.evaluate(sample);

		if (minValue > evaluation.getValue())
		{
			throw new Rage4JRelevanceException("Answer did not reach required min value! Evaluated value: "
				+ evaluation.getValue() + ", Required: " + minValue + ", Answer: " + answer);
		}
		return evaluation;
	}

	public Evaluation assertSemanticSimilarity(double minValue)
	{
		AnswerSemanticSimilarityEvaluator evaluator = new AnswerSemanticSimilarityEvaluator(embeddingModel);
		Sample sample = Sample.builder()
			.withAnswer(answer)
			.withGroundTruth(groundTruth)
			.build();
		Evaluation evaluation = evaluator.evaluate(sample);

		if (minValue > evaluation.getValue())
		{
			throw new Rage4JSimilarityException("Answer did not reach required min value! Evaluated value: "
				+ evaluation.getValue() + ", Required: " + minValue + ", Answer: " + answer);
		}
		return evaluation;
	}
}
