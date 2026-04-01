package dev.rage4j.asserts;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.rage4j.asserts.exception.Rage4JBleuScoreException;
import dev.rage4j.asserts.exception.Rage4JCorrectnessException;
import dev.rage4j.asserts.exception.Rage4JDirectBiasFairnessException;
import dev.rage4j.asserts.exception.Rage4JFaithfulnessException;
import dev.rage4j.asserts.exception.Rage4JFalsePositiveRateDisparityException;
import dev.rage4j.asserts.exception.Rage4JLocalGenderBiasException;
import dev.rage4j.asserts.exception.Rage4JNameBasedApplicationScoringException;
import dev.rage4j.asserts.exception.Rage4JRelevanceException;
import dev.rage4j.asserts.exception.Rage4JRefusalException;
import dev.rage4j.asserts.exception.Rage4JRougeScoreException;
import dev.rage4j.asserts.exception.Rage4JSimilarityException;
import dev.rage4j.asserts.exception.Rage4JStereotypicalAssociationsException;
import dev.rage4j.evaluation.Evaluation;
import dev.rage4j.evaluation.answercorrectness.AnswerCorrectnessEvaluator;
import dev.rage4j.evaluation.answerrelevance.AnswerRelevanceEvaluator;
import dev.rage4j.evaluation.answersemanticsimilarity.AnswerSemanticSimilarityEvaluator;
import dev.rage4j.evaluation.bleuscore.BleuScoreEvaluator;
import dev.rage4j.evaluation.bias.DirectBiasFairness.support.DirectBiasFairnessBatchResult;
import dev.rage4j.evaluation.bias.DirectBiasFairness.DirectBiasFairnessEvaluator;
import dev.rage4j.evaluation.bias.FalsePositiveRateDisparity.FalsePositiveRateDisparityEvaluator;
import dev.rage4j.evaluation.bias.Refusal.RefusalEvaluator;
import dev.rage4j.evaluation.bias.LocalGender.LocalGenderBiasEvaluator;
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
	private String comparisonQuestion;
	private String comparisonGroundTruth;
	private List<String> comparisonContextList;
	private String comparisonAnswer;

	private static final String MINVALUE = "Answer did not reach required min value! Evaluated value: ";
	private static final String MAXVALUE = "Answer exceeded allowed max value! Evaluated value: ";
	private static final String ABSOLUTE_MAXVALUE = "Answer exceeded allowed max absolute value! Evaluated value: ";

	public RageAssertTestCaseAssertions(
		String answer,
		String groundTruth,
		String question,
		List<String> contextList,
		String comparisonAnswer,
		String comparisonGroundTruth,
		String comparisonQuestion,
		List<String> comparisonContextList,
		ChatModel chatModel,
		EmbeddingModel embeddingModel)
	{
		this.answer = answer;
		this.groundTruth = groundTruth;
		this.question = question;
		this.contextList = contextList;
		this.comparisonAnswer = comparisonAnswer;
		this.comparisonGroundTruth = comparisonGroundTruth;
		this.comparisonQuestion = comparisonQuestion;
		this.comparisonContextList = comparisonContextList;
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

	public AssertionEvaluation assertRefusalDisparityRegex(double maxValue)
	{
		RefusalEvaluator evaluator = new RefusalEvaluator();
		Sample sample = buildPairedSample();
		Evaluation evaluation = evaluator.evaluate(sample);

		if (evaluation.getValue() > maxValue)
		{
			throw new Rage4JRefusalException(MAXVALUE + evaluation.getValue() + ", Allowed: " + maxValue);
		}
		return AssertionEvaluation.from(evaluation, this);
	}

	public AssertionEvaluation assertRefusalDisparity(double maxValue)
	{
		ChatModel judgeModel = requireChatModel("Refusal disparity assertion requires a chat model.");
		RefusalEvaluator evaluator = new RefusalEvaluator(null, judgeModel);
		Sample sample = buildPairedSample();
		Evaluation evaluation = evaluator.evaluate(sample);

		if (evaluation.getValue() > maxValue)
		{
			throw new Rage4JRefusalException(MAXVALUE + evaluation.getValue() + ", Allowed: " + maxValue);
		}
		return AssertionEvaluation.from(evaluation, this);
	}

	public AssertionEvaluation assertFalsePositiveRateDisparity(double maxValue)
	{
		ChatModel judgeModel = requireChatModel("False Positive Rate Disparity assertion requires a chat model.");
		FalsePositiveRateDisparityEvaluator evaluator = new FalsePositiveRateDisparityEvaluator(judgeModel);
		Sample sample = buildPairedSample();
		Evaluation evaluation = evaluator.evaluate(sample);

		if (evaluation.getValue() > maxValue)
		{
			throw new Rage4JFalsePositiveRateDisparityException(
				MAXVALUE + evaluation.getValue() + ", Allowed: " + maxValue);
		}
		return AssertionEvaluation.from(evaluation, this);
	}

	public AssertionEvaluation assertNameBasedApplicationScoreDifference(double maxAbsoluteDifference)
	{
		ChatModel judgeModel = requireChatModel("Name-based application scoring assertion requires a chat model.");
		NameBasedApplicationScoringEvaluator evaluator = new NameBasedApplicationScoringEvaluator(judgeModel);
		Sample sample = buildPairedSample();
		Evaluation evaluation = evaluator.evaluate(sample);

		if (Math.abs(evaluation.getValue()) > maxAbsoluteDifference)
		{
			throw new Rage4JNameBasedApplicationScoringException(
				ABSOLUTE_MAXVALUE + evaluation.getValue() + ", Allowed: " + maxAbsoluteDifference);
		}
		return AssertionEvaluation.from(evaluation, this);
	}

	public AssertionEvaluation assertStereotypicalAssociations(double maxValue)
	{
		ChatModel judgeModel = requireChatModel("Stereotypical associations assertion requires a chat model.");
		StereotypicalAssociationsEvaluator evaluator = new StereotypicalAssociationsEvaluator(judgeModel);
		Sample sample = buildSingleSample();
		Evaluation evaluation = evaluator.evaluate(sample);

		if (evaluation.getValue() > maxValue)
		{
			throw new Rage4JStereotypicalAssociationsException(
				MAXVALUE + evaluation.getValue() + ", Allowed: " + maxValue);
		}
		return AssertionEvaluation.from(evaluation, this);
	}

	public AssertionEvaluation assertLocalGenderBias(double maxAbsoluteValue)
	{
		LocalGenderBiasEvaluator evaluator = new LocalGenderBiasEvaluator();
		Sample sample = buildSingleSample();
		Evaluation evaluation = evaluator.evaluate(sample);

		if (Math.abs(evaluation.getValue()) > maxAbsoluteValue)
		{
			throw new Rage4JLocalGenderBiasException(
				ABSOLUTE_MAXVALUE + evaluation.getValue() + ", Allowed: " + maxAbsoluteValue);
		}
		return AssertionEvaluation.from(evaluation, this);
	}

	public AssertionEvaluation assertDirectBiasFairness(String category, double maxAbsoluteDifference)
	{
		ChatModel evaluatedModel = requireChatModel("Direct bias fairness assertion requires a chat model.");
		DirectBiasFairnessEvaluator evaluator = new DirectBiasFairnessEvaluator(category);
		DirectBiasFairnessBatchResult batchResult = evaluator.evaluate(evaluatedModel);
		Evaluation evaluation = new Evaluation(
			"Direct Bias Fairness " + category,
			batchResult.averageScoreDifference());

		if (Math.abs(evaluation.getValue()) > maxAbsoluteDifference)
		{
			throw new Rage4JDirectBiasFairnessException(
				ABSOLUTE_MAXVALUE + evaluation.getValue() + ", Allowed: " + maxAbsoluteDifference);
		}
		return AssertionEvaluation.from(evaluation, this);
	}

	private ChatModel requireChatModel(String message)
	{
		if (chatModel == null)
		{
			throw new IllegalStateException(message);
		}
		return chatModel;
	}

	private Sample buildSingleSample()
	{
		Sample.SampleBuilder builder = Sample.builder()
			.withAnswer(answer)
			.withQuestion(question);

		if (groundTruth != null)
		{
			builder.withGroundTruth(groundTruth);
		}
		if (contextList != null)
		{
			builder.withContextsList(contextList);
		}
		return builder.build();
	}

	private Sample buildPairedSample()
	{
		requireComparisonSample();

		Sample.SampleBuilder comparisonBuilder = Sample.builder()
			.withAnswer(comparisonAnswer)
			.withQuestion(comparisonQuestion);
		if (comparisonGroundTruth != null)
		{
			comparisonBuilder.withGroundTruth(comparisonGroundTruth);
		}
		if (comparisonContextList != null)
		{
			comparisonBuilder.withContextsList(comparisonContextList);
		}

		Sample comparisonSample = comparisonBuilder.build();
		Sample.SampleBuilder mainBuilder = Sample.builder()
			.withAnswer(answer)
			.withQuestion(question)
			.withComparisonSample(comparisonSample);
		if (groundTruth != null)
		{
			mainBuilder.withGroundTruth(groundTruth);
		}
		if (contextList != null)
		{
			mainBuilder.withContextsList(contextList);
		}
		return mainBuilder.build();
	}

	private void requireComparisonSample()
	{
		if (comparisonQuestion == null || comparisonQuestion.trim().isEmpty())
		{
			throw new IllegalStateException("Comparison question is required for paired bias assertions.");
		}
		if (comparisonAnswer == null)
		{
			throw new IllegalStateException("Comparison answer is required for paired bias assertions.");
		}
		if (answer == null)
		{
			throw new IllegalStateException("Answer is required for paired bias assertions.");
		}
	}
}
