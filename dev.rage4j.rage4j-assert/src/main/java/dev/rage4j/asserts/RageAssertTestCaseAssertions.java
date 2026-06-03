package dev.rage4j.asserts;

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
import dev.rage4j.evaluation.answerrelevance.embedding.AnswerRelevanceEmbeddingEvaluator;
import dev.rage4j.evaluation.answersemanticsimilarity.AnswerSemanticSimilarityEvaluator;
import dev.rage4j.evaluation.bleuscore.BleuScoreEvaluator;
import dev.rage4j.evaluation.faithfulness.FaithfulnessEvaluator;
import dev.rage4j.evaluation.rougescore.RougeScoreEvaluator;
import dev.rage4j.model.EvaluationAggregation;
import dev.rage4j.model.Sample;

public class RageAssertTestCaseAssertions
{
	private static final Logger LOG = LoggerFactory.getLogger(RageAssertTestCaseAssertions.class);

	private final Sample sample;
	private final ChatModel chatModel;
	private final EmbeddingModel embeddingModel;
	private final boolean evaluationMode;
	private EvaluationAggregation pendingAggregation;

	private static final String MINVALUE = "Answer did not reach required min value! Evaluated value: ";

	public RageAssertTestCaseAssertions(Sample sample, ChatModel chatModel, EmbeddingModel embeddingModel, boolean evaluationMode)
	{
		this.sample = sample;
		this.chatModel = chatModel;
		this.embeddingModel = embeddingModel;
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

	private void collectEvaluation(Evaluation evaluation)
	{
		if (pendingAggregation == null)
		{
			pendingAggregation = new EvaluationAggregation(sample);
		}
		pendingAggregation.put(evaluation);
	}

	/**
	 * Returns the collected evaluation aggregation containing the sample and
	 * all evaluation results performed so far.
	 *
	 * @return The EvaluationAggregation with sample and metrics, or null if no
	 *         evaluations were performed.
	 */
	public EvaluationAggregation getEvaluationAggregation()
	{
		return pendingAggregation;
	}

	public AssertionEvaluation assertFaithfulness(double minValue)
	{
		FaithfulnessEvaluator evaluator = new FaithfulnessEvaluator(chatModel, sample.hasImages());
		Evaluation evaluation = evaluator.evaluate(sample);

		boolean passed = minValue <= evaluation.getValue();
		collectEvaluation(evaluation);

		if (!passed)
		{
			String message = MINVALUE + evaluation.getValue() + " answer: " + sample.getAnswer();
			handleAssertionFailure(message, "Faithfulness", () -> new Rage4JFaithfulnessException(message));
		}
		return AssertionEvaluation.from(evaluation, this);
	}

	public AssertionEvaluation assertAnswerCorrectness(double minValue)
	{
		AnswerCorrectnessEvaluator evaluator = new AnswerCorrectnessEvaluator(chatModel);
		Evaluation evaluation = evaluator.evaluate(sample);

		boolean passed = minValue <= evaluation.getValue();
		collectEvaluation(evaluation);

		if (!passed)
		{
			String message = MINVALUE + evaluation.getValue() + " answer: " + sample.getAnswer();
			handleAssertionFailure(message, "Answer Correctness", () -> new Rage4JCorrectnessException(message));
		}
		return AssertionEvaluation.from(evaluation, this);
	}

	public AssertionEvaluation assertAnswerRelevance(double minValue)
	{
		AnswerRelevanceEmbeddingEvaluator evaluator = new AnswerRelevanceEmbeddingEvaluator(chatModel, embeddingModel);
		Evaluation evaluation = evaluator.evaluate(sample);

		boolean passed = minValue <= evaluation.getValue();
		collectEvaluation(evaluation);

		if (!passed)
		{
			String message = assertionFailureMessage(evaluation.getValue(), minValue, sample.getAnswer());
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
		Evaluation evaluation = evaluator.evaluate(sample);

		boolean passed = minValue <= evaluation.getValue();
		collectEvaluation(evaluation);

		if (!passed)
		{
			String message = assertionFailureMessage(evaluation.getValue(), minValue, sample.getAnswer());
			handleAssertionFailure(message, "Semantic Similarity", () -> new Rage4JSimilarityException(message));
		}
		return AssertionEvaluation.from(evaluation, this);
	}

	public AssertionEvaluation assertBleuScore(double minValue)
	{
		BleuScoreEvaluator evaluator = new BleuScoreEvaluator();
		Evaluation evaluation = evaluator.evaluate(sample);

		boolean passed = minValue <= evaluation.getValue();
		collectEvaluation(evaluation);

		if (!passed)
		{
			String message = assertionFailureMessage(evaluation.getValue(), minValue, sample.getAnswer());
			handleAssertionFailure(message, "BLEU Score", () -> new Rage4JBleuScoreException(message));
		}
		return AssertionEvaluation.from(evaluation, this);
	}

	public AssertionEvaluation assertRougeScore(double minValue, RougeScoreEvaluator.RougeType rougeType, RougeScoreEvaluator.MeasureType measureType)
	{
		RougeScoreEvaluator evaluator = new RougeScoreEvaluator(rougeType, measureType);
		Evaluation evaluation = evaluator.evaluate(sample);

		boolean passed = minValue <= evaluation.getValue();
		collectEvaluation(evaluation);

		if (!passed)
		{
			String message = assertionFailureMessage(evaluation.getValue(), minValue, sample.getAnswer());
			handleAssertionFailure(message, "ROUGE Score", () -> new Rage4JRougeScoreException(message));
		}
		return AssertionEvaluation.from(evaluation, this);
	}
}