package dev.rage4j.evaluation.rougescore;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.rage4j.evaluation.Evaluation;
import dev.rage4j.evaluation.Evaluator;
import dev.rage4j.evaluation.rougescore.model.Measurement;
import dev.rage4j.model.Sample;
import dev.rage4j.util.StringSimilarityComputer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BiFunction;

import static dev.rage4j.evaluation.rougescore.rougemetrics.RougeL.calculateRougeL;
import static dev.rage4j.evaluation.rougescore.rougemetrics.RougeL.calculateRougeLsum;
import static dev.rage4j.evaluation.rougescore.rougemetrics.RougeN.calculateRougeN;

/**
 * The {@code RougeScoreEvaluator} class implements the ROUGE (Recall-Oriented Understudy for Gisting Evaluation) metric for evaluating the quality of generated text against reference text.
 */
public class RougeScoreEvaluator implements Evaluator
{
	private static final String METRIC_NAME = "ROUGE score";
	private static final double EPSILON = 1e-8;
	private final BiFunction<String, String, Double> stringSimilarityComputer;

	private static final Logger LOG = LoggerFactory.getLogger(RougeScoreEvaluator.class);

	private final RougeType rougeType;
	private final MeasureType measureType;

	public enum RougeType
	{
		ROUGE1, ROUGE2, ROUGE_L, ROUGE_L_SUM
	}

	public enum MeasureType
	{
		PRECISION, RECALL, F1SCORE
	}

	public RougeScoreEvaluator(EmbeddingModel embeddingModel)
	{
		this(embeddingModel, RougeType.ROUGE1, MeasureType.F1SCORE);
	}

	public RougeScoreEvaluator(EmbeddingModel embeddingModel, RougeType rougeType, MeasureType measureType)
	{
		this.stringSimilarityComputer = new StringSimilarityComputer(embeddingModel);
		this.rougeType = rougeType;
		this.measureType = measureType;
	}

	public RougeScoreEvaluator()
	{
		this(null, RougeType.ROUGE1, MeasureType.F1SCORE);
	}

	public RougeScoreEvaluator(RougeType rougeType, MeasureType measureType)
	{
		this.stringSimilarityComputer = null;
		this.rougeType = rougeType;
		this.measureType = measureType;
	}

	/**
	 * Evaluates a sample by comparing the candidate text against the reference text using ROUGE metrics.
	 *
	 * @param sample
	 * 	The sample containing both the candidate and reference texts
	 * @return An Evaluation object containing the ROUGE score
	 */
	@Override
	public Evaluation evaluate(Sample sample)
	{
		String answer = sample.getAnswerOrFail();
		String groundTruth = sample.getGroundTruthOrFail();
		LOG.info("Evaluating new sample");
		LOG.info("Ground truth: {}", groundTruth);
		LOG.info("Answer: {}", answer);

		double score = calculateRougeScore(answer, groundTruth);
		LOG.info("ROUGE score ({}): {}", rougeType, score);
		return new Evaluation(METRIC_NAME + " " + rougeType, score);
	}

	/**
	 * Calculates the appropriate ROUGE score based on the configured rouge type.
	 *
	 * @param candidate
	 * 	The generated text to evaluate
	 * @param reference
	 * 	The reference text to compare against
	 * @return The calculated ROUGE score as a double
	 */
	private double calculateRougeScore(String candidate, String reference)
	{
		String[] candidateTokens = tokenize(candidate);
		String[] referenceTokens = tokenize(reference);

		return switch (rougeType)
		{
			case ROUGE1 -> getScore(calculateRougeN(candidateTokens, referenceTokens, 1));
			case ROUGE2 -> getScore(calculateRougeN(candidateTokens, referenceTokens, 2));
			case ROUGE_L -> getScore(calculateRougeL(candidateTokens, referenceTokens));
			case ROUGE_L_SUM -> getScore(calculateRougeLsum(candidateTokens, referenceTokens));
			default -> throw new IllegalStateException("Unsupported ROUGE type: " + rougeType);
		};
	}

	private String[] tokenize(String text)
	{
		if (rougeType == RougeType.ROUGE_L_SUM)
		{
			text = text.replaceAll("\n", " \n ");
			return text.split("[ \\t\\x0B\\f\\r]+");
		}
		return text.split("\\s+");
	}

	/**
	 * Calculates the F1 score based on the provided precision and recall values.
	 *
	 * @param measurement
	 * 	Measurement containing precision and recall values
	 * @return Returns the F1 score
	 */
	private double getScore(Measurement measurement)
	{
		double precision = measurement.getPrecision();
		double recall = measurement.getRecall();

		return switch (measureType)
		{
			case PRECISION -> precision;
			case RECALL -> recall;
			case F1SCORE ->
			{
				if (precision + recall < EPSILON)
				{
					yield 0.0;
				}
				yield 2.0 * (precision * recall) / (precision + recall + EPSILON);
			}
			default -> throw new IllegalStateException("Unsupported measure type: " + measureType);
		};
	}
}