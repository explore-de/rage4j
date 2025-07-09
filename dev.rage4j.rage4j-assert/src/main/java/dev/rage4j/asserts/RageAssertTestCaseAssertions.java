package dev.rage4j.asserts;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.rage4j.asserts.exception.Rage4JBleuScoreException;
import dev.rage4j.asserts.exception.Rage4JCorrectnessException;
import dev.rage4j.asserts.exception.Rage4JFaithfulnessException;
import dev.rage4j.asserts.exception.Rage4JRelevanceException;
import dev.rage4j.asserts.exception.Rage4JRougeScoreException;
import dev.rage4j.asserts.exception.Rage4JSimilarityException;
import dev.rage4j.evaluation.Evaluation;
import dev.rage4j.evaluation.answercorrectness.AnswerCorrectnessEvaluator;
import dev.rage4j.evaluation.answerrelevance.AnswerRelevanceEvaluator;
import dev.rage4j.evaluation.answersemanticsimilarity.AnswerSemanticSimilarityEvaluator;
import dev.rage4j.evaluation.bleuscore.BleuScoreEvaluator;
import dev.rage4j.evaluation.faithfulness.FaithfulnessEvaluator;
import dev.rage4j.evaluation.rougescore.RougeScoreEvaluator;
import dev.rage4j.model.Sample;

import java.util.List;

public class RageAssertTestCaseAssertions
{
	private ChatModel chatModel;
	private EmbeddingModel embeddingModel;
	private String question;
	private String groundTruth;
	private List<String> contextList;
	private String answer;

	private static final String MINVALUE = "Answer did not reach required min value! Evaluated value: ";

	public RageAssertTestCaseAssertions(String answer, String groundTruth, String question, List<String> contextList, ChatModel chatModel, EmbeddingModel embeddingModel)
	{
		this.answer = answer;
		this.groundTruth = groundTruth;
		this.question = question;
		this.contextList = contextList;
		this.chatModel = chatModel;
		this.embeddingModel = embeddingModel;
	}

	public AssertionEvaluation assertFaithfulness(double minValue)
	{
		FaithfulnessEvaluator evaluator = new FaithfulnessEvaluator(chatModel);
		Sample sample = Sample.builder()
			.withAnswer(answer)
			.withGroundTruth(groundTruth)
			.withQuestion(question)
			.withContextsList(contextList)
			.build();
		Evaluation evaluation = evaluator.evaluate(sample);

		if (minValue > evaluation.getValue())
		{
			throw new Rage4JFaithfulnessException(MINVALUE + evaluation.getValue() + " answer: " + answer);
		}
		return AssertionEvaluation.from(evaluation, this);
	}

	public AssertionEvaluation assertAnswerCorrectness(double minValue)
	{
		AnswerCorrectnessEvaluator evaluator = new AnswerCorrectnessEvaluator(chatModel);
		Sample sample = Sample.builder()
			.withAnswer(answer)
			.withGroundTruth(groundTruth)
			.withQuestion(question)
			.build();
		Evaluation evaluation = evaluator.evaluate(sample);

		if (minValue > evaluation.getValue())
		{
			throw new Rage4JCorrectnessException(MINVALUE + evaluation.getValue() + " answer: " + answer);
		}
		return AssertionEvaluation.from(evaluation, this);
	}

	public AssertionEvaluation assertAnswerRelevance(double minValue)
	{
		AnswerRelevanceEvaluator evaluator = new AnswerRelevanceEvaluator(chatModel, embeddingModel);
		Sample sample = Sample.builder()
			.withAnswer(answer)
			.withQuestion(question)
			.withContextsList(contextList)
			.build();
		Evaluation evaluation = evaluator.evaluate(sample);

		if (minValue > evaluation.getValue())
		{
			throw new Rage4JRelevanceException(MINVALUE + evaluation.getValue() + ", Required: " + minValue + ", Answer: " + answer);
		}
		return AssertionEvaluation.from(evaluation, this);
	}

	public AssertionEvaluation assertSemanticSimilarity(double minValue)
	{
		AnswerSemanticSimilarityEvaluator evaluator = new AnswerSemanticSimilarityEvaluator(embeddingModel);
		Sample sample = Sample.builder()
			.withAnswer(answer)
			.withGroundTruth(groundTruth)
			.build();
		Evaluation evaluation = evaluator.evaluate(sample);

		if (minValue > evaluation.getValue())
		{
			throw new Rage4JSimilarityException(MINVALUE + evaluation.getValue() + ", Required: " + minValue + ", Answer: " + answer);
		}
		return AssertionEvaluation.from(evaluation, this);
	}

	public AssertionEvaluation assertBleuScore(double minValue)
	{
		BleuScoreEvaluator evaluator = new BleuScoreEvaluator();
		Sample sample = Sample.builder()
			.withAnswer(answer)
			.withGroundTruth(groundTruth)
			.build();
		Evaluation evaluation = evaluator.evaluate(sample);

		if (minValue > evaluation.getValue())
		{
			throw new Rage4JBleuScoreException(MINVALUE + evaluation.getValue() + ", Required: " + minValue + ", Answer: " + answer);
		}
		return AssertionEvaluation.from(evaluation, this);
	}

	public AssertionEvaluation assertRougeScore(double minValue, RougeScoreEvaluator.RougeType rougeType, RougeScoreEvaluator.MeasureType measureType)
	{
		RougeScoreEvaluator evaluator = new RougeScoreEvaluator(rougeType, measureType);
		Sample sample = Sample.builder()
			.withAnswer(answer)
			.withGroundTruth(groundTruth)
			.build();
		Evaluation evaluation = evaluator.evaluate(sample);

		if (minValue > evaluation.getValue())
		{
			throw new Rage4JRougeScoreException(MINVALUE + evaluation.getValue() + ", Required: " + minValue + ", Answer: " + answer);
		}
		return AssertionEvaluation.from(evaluation, this);
	}
}
