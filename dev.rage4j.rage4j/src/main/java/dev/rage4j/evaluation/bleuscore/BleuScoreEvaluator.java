package dev.rage4j.evaluation.bleuscore;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.rage4j.evaluation.Evaluation;
import dev.rage4j.evaluation.Evaluator;
import dev.rage4j.model.Sample;
import dev.rage4j.util.StringSimilarityComputer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.function.BiFunction;

import static dev.rage4j.util.NGramUtils.getNGramCounts;

/**
 * The {@code BleuScoreEvaluator} class implements the BLEU (Bilingual Evaluation Understudy) metric for evaluating the quality of generated text against reference text. It compares n-gram matches between candidate and reference texts to assess translation/generation quality.
 * <p>
 * The result is a score between 0 and 1, where 1.0 indicates perfect match with reference 0.0 indicates no matching n-grams.
 */
public class BleuScoreEvaluator implements Evaluator
{
	//todo maybe implement sacrebleu
	//todo allow for multiple answers

	//todo maybe allow the user to define these constant values in the function call
	private static final String METRIC_NAME = "BLEU score";
	private static final double EPSILON = 1e-10;
	private final BiFunction<String, String, Double> stringSimilarityComputer;

	// maximum n-gram size is 4, with all of them weighted equally
	private static final int MAX_NGRAM = 4;
	private static final double[] WEIGHTS = { 0.25, 0.25, 0.25, 0.25 };

	private static final Logger LOG = LoggerFactory.getLogger(BleuScoreEvaluator.class);

	public BleuScoreEvaluator(EmbeddingModel embeddingModel)
	{
		this.stringSimilarityComputer = new StringSimilarityComputer(embeddingModel);
	}

	public BleuScoreEvaluator()
	{
		this.stringSimilarityComputer = null;
	}

	/**
	 * Evaluates the quality of generated text against a reference text using the BLEU metric.
	 *
	 * @param sample
	 * 	The sample containing both the generated answer and ground truth
	 * @return An Evaluation object containing the BLEU score
	 */
	@Override
	public Evaluation evaluate(Sample sample)
	{
		String answer = sample.getAnswerOrFail();
		String groundTruth = sample.getGroundTruthOrFail();
		LOG.info("Evaluating new sample");
		LOG.info("Ground truth: {}", groundTruth);
		LOG.info("Answer: {}", answer);

		double score = calculateBleuScore(answer, groundTruth);
		LOG.info("BLEU score: {}", score);
		return new Evaluation(METRIC_NAME, score);
	}

	/**
	 * Calculates the BLEU score between a candidate and reference text.
	 *
	 * @param candidate
	 * 	The generated text to evaluate
	 * @param reference
	 * 	The ground truth text to compare against
	 * @return A score between 0 and 1, where 1 indicates perfect match
	 */
	private double calculateBleuScore(String candidate, String reference)
	{
		//todo use a tokenizer here
		String[] candidateWords = candidate.toLowerCase().split("\\s+");
		String[] referenceWords = reference.toLowerCase().split("\\s+");

		double[] precisions = new double[MAX_NGRAM];
		for (int n = 0; n < MAX_NGRAM; n++)
		{
			precisions[n] = calculateClippedNGramPrecision(candidateWords, referenceWords, n + 1);
		}

		boolean allZeros = true;
		for (double precision : precisions)
		{
			if (precision > 0)
			{
				allZeros = false;
				break;
			}
		}
		if (allZeros) return 0.0;

		double weightedLogPrecision = 0.0;
		for (int i = 0; i < MAX_NGRAM; i++)
		{
			double smoothedPrecision = precisions[i] + EPSILON;
			weightedLogPrecision += WEIGHTS[i] * Math.log(smoothedPrecision);
		}

		if (candidateWords.length >= referenceWords.length)
		{
			return 1.0;
		}

		double brevityPenalty = Math.exp(1 - ((double)referenceWords.length / candidateWords.length));

		return brevityPenalty * Math.exp(weightedLogPrecision);
	}

	/**
	 * Calculates the clipped n-gram precision for a specific n-gram size. Clipping ensures that we don't over-count n-grams that appear more times in the candidate than in the reference.
	 *
	 * @param candidate
	 * 	Array of words from candidate text
	 * @param reference
	 * 	Array of words from reference text
	 * @param n
	 * 	The n-gram size to evaluate
	 * @return The clipped precision score for the given n-gram size
	 */
	private double calculateClippedNGramPrecision(String[] candidate, String[] reference, int n)
	{
		if (candidate.length < n || reference.length < n)
		{
			return 0.0;
		}

		Map<String, Integer> candidateCounts = getNGramCounts(candidate, n);
		Map<String, Integer> referenceCounts = getNGramCounts(reference, n);

		int clippedCount = 0;
		int totalCount = 0;

		for (Map.Entry<String, Integer> entry : candidateCounts.entrySet())
		{
			String ngram = entry.getKey();
			int candCount = entry.getValue();
			int refCount = referenceCounts.getOrDefault(ngram, 0);
			clippedCount += Math.min(candCount, refCount);
			totalCount += candCount;
		}

		return totalCount == 0 ? 0.0 : (double)clippedCount / totalCount;
	}
}