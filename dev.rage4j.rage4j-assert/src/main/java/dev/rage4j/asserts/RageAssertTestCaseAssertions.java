package dev.rage4j.asserts;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

public class RageAssertTestCaseAssertions
{
	private static final Logger LOG = LoggerFactory.getLogger(RageAssertTestCaseAssertions.class);

	private final ChatModel chatModel;
	private final EmbeddingModel embeddingModel;
	private final String question;
	private final String groundTruth;
	private final List<String> contextList;
	private final String answer;
	private final List<AssertionObserver> observers;
	private final boolean evaluationMode;

	private static final String MINVALUE = "Answer did not reach required min value! Evaluated value: ";

	public RageAssertTestCaseAssertions(String answer, String groundTruth, String question, List<String> contextList, ChatModel chatModel, EmbeddingModel embeddingModel, List<AssertionObserver> observers, boolean evaluationMode)
	{
		this.answer = answer;
		this.groundTruth = groundTruth;
		this.question = question;
		this.contextList = contextList;
		this.chatModel = chatModel;
		this.embeddingModel = embeddingModel;
		this.observers = observers != null ? observers : Collections.emptyList();
		this.evaluationMode = evaluationMode;
	}

	/**
	 * Handles assertion failure based on evaluation mode. In evaluation mode,
	 * logs a warning. Otherwise, throws the provided exception.
	 *
	 * @param message
	 *            The failure message.
	 * @param metricName
	 *            The name of the metric that failed.
	 * @param exceptionSupplier
	 *            Supplier that creates the exception to throw.
	 */
	private void handleAssertionFailure(String message, String metricName, Supplier<RuntimeException> exceptionSupplier)
	{
		if (evaluationMode)
		{
			LOG.warn("[EVALUATION MODE] {} assertion failed: {}", metricName, message);
		}
		else
		{
			throw exceptionSupplier.get();
		}
	}

	private void notifyObservers(Sample sample, Evaluation evaluation, boolean passed)
	{
		for (AssertionObserver observer : observers)
		{
			observer.onEvaluation(sample, evaluation, passed);
		}
	}

	private String joinContextList()
	{
		if (contextList == null || contextList.isEmpty())
		{
			return null;
		}
		return String.join("\n", contextList);
	}

	public AssertionEvaluation assertFaithfulness(double minValue)
	{
		FaithfulnessEvaluator evaluator = new FaithfulnessEvaluator(chatModel);
		Sample sample = Sample.builder()
			.withAnswer(answer)
			.withGroundTruth(groundTruth)
			.withQuestion(question)
			.withContext(joinContextList())
			.build();
		Evaluation evaluation = evaluator.evaluate(sample);

		boolean passed = minValue <= evaluation.getValue();
		notifyObservers(sample, evaluation, passed);

		if (!passed)
		{
			String message = MINVALUE + evaluation.getValue() + " answer: " + answer;
			handleAssertionFailure(message, "Faithfulness", () -> new Rage4JFaithfulnessException(message));
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

		boolean passed = minValue <= evaluation.getValue();
		notifyObservers(sample, evaluation, passed);

		if (!passed)
		{
			String message = MINVALUE + evaluation.getValue() + " answer: " + answer;
			handleAssertionFailure(message, "Answer Correctness", () -> new Rage4JCorrectnessException(message));
		}
		return AssertionEvaluation.from(evaluation, this);
	}

	public AssertionEvaluation assertAnswerRelevance(double minValue)
	{
		AnswerRelevanceEvaluator evaluator = new AnswerRelevanceEvaluator(chatModel, embeddingModel);
		Sample sample = Sample.builder()
			.withAnswer(answer)
			.withQuestion(question)
			.withContext(joinContextList())
			.build();
		Evaluation evaluation = evaluator.evaluate(sample);

		boolean passed = minValue <= evaluation.getValue();
		notifyObservers(sample, evaluation, passed);

		if (!passed)
		{
			String message = assertionFailureMessage(evaluation.getValue(), minValue, answer);
			handleAssertionFailure(message, "Answer Relevance", () -> new Rage4JRelevanceException(message));
		}
		return AssertionEvaluation.from(evaluation, this);
	}

	private String assertionFailureMessage(double value, double minValue, String answer)
	{
		return MINVALUE + value + ", Required: " + minValue + ", Answer: " + answer;
	}

	public AssertionEvaluation assertSemanticSimilarity(double minValue)
	{
		AnswerSemanticSimilarityEvaluator evaluator = new AnswerSemanticSimilarityEvaluator(embeddingModel);
		Sample sample = Sample.builder()
			.withAnswer(answer)
			.withGroundTruth(groundTruth)
			.build();
		Evaluation evaluation = evaluator.evaluate(sample);

		boolean passed = minValue <= evaluation.getValue();
		notifyObservers(sample, evaluation, passed);

		if (!passed)
		{
			String message = assertionFailureMessage(evaluation.getValue(), minValue, answer);
			handleAssertionFailure(message, "Semantic Similarity", () -> new Rage4JSimilarityException(message));
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

		boolean passed = minValue <= evaluation.getValue();
		notifyObservers(sample, evaluation, passed);

		if (!passed)
		{
			String message = assertionFailureMessage(evaluation.getValue(), minValue, answer);
			handleAssertionFailure(message, "BLEU Score", () -> new Rage4JBleuScoreException(message));
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

		boolean passed = minValue <= evaluation.getValue();
		notifyObservers(sample, evaluation, passed);

		if (!passed)
		{
			String message = assertionFailureMessage(evaluation.getValue(), minValue, answer);
			handleAssertionFailure(message, "ROUGE Score", () -> new Rage4JRougeScoreException(message));
		}
		return AssertionEvaluation.from(evaluation, this);
	}
}
