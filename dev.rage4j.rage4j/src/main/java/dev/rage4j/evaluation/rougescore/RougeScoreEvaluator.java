package dev.rage4j.evaluation.rougescore;

import dev.rage4j.evaluation.Evaluation;
import dev.rage4j.evaluation.Evaluator;
import dev.rage4j.model.Sample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The {@code RougeScoreEvaluator} class implements the ROUGE (Recall-Oriented Understudy for Gisting Evaluation) metric for evaluating the quality of generated text against reference text.
 */
public class RougeScoreEvaluator implements Evaluator
{
	private static final String METRIC_NAME = "ROUGE score";
	private static final double EPSILON = 1e-8;

	private static final Logger LOG = LoggerFactory.getLogger(RougeScoreEvaluator.class);

	private final RougeType rougeType;
	private final MeasureType measureType;

	public enum RougeType
	{
		ROUGE1, ROUGE2
	}

	public enum MeasureType
	{
		PRECISION, RECALL, F1SCORE
	}

	// use ROUGE1 and F1-Score by default
	public RougeScoreEvaluator()
	{
		this(RougeType.ROUGE1, MeasureType.F1SCORE);
	}

	public RougeScoreEvaluator(RougeType rougeType, MeasureType measureType)
	{
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
		String candidate = sample.getAnswerOrFail();
		String reference = sample.getGroundTruthOrFail();

		LOG.info("Evaluating new sample");
		LOG.info("Ground truth: {}", reference);
		LOG.info("Answer: {}", candidate);

		double score = calculateRougeScore(candidate, reference);
		LOG.info("ROUGE score: {}", score);
		return new Evaluation(METRIC_NAME, score);
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
		String[] candidateTokens = candidate.toLowerCase().split("\\s+");
		String[] referenceTokens = reference.toLowerCase().split("\\s+");

		switch (rougeType)
		{
			case ROUGE1:
				return calculateRougeN(candidateTokens, referenceTokens, 1);
			case ROUGE2:
				return calculateRougeN(candidateTokens, referenceTokens, 2);
			default:
				throw new IllegalStateException("Unsupported ROUGE type: " + rougeType);
		}
	}

	/**
	 * Calculates ROUGE-N score by comparing n-grams between candidate and reference texts.
	 *
	 * @param candidate
	 * 	Array of tokens from the candidate text
	 * @param reference
	 * 	Array of tokens from the reference text
	 * @param n
	 * 	The size of n-grams to consider (1 for unigrams, 2 for bigrams, etc.)
	 * @return The ROUGE-N score based on the configured measure type
	 */
	private double calculateRougeN(String[] candidate, String[] reference, int n)
	{
		if (candidate.length < n || reference.length < n)
		{
			return 0.0;
		}

		List<String[]> candidateNgrams = getNGrams(candidate, n);
		List<String[]> referenceNgrams = getNGrams(reference, n);

		int overlap = calculateNgramOverlap(candidateNgrams, referenceNgrams);

		double precision = (double)overlap / candidateNgrams.size();
		double recall = (double)overlap / referenceNgrams.size();

		return getScore(precision, recall);
	}

	/**
	 * Generates n-grams from an array of tokens.
	 *
	 * @param tokens
	 * 	The input array of tokens
	 * @param n
	 * 	The size of n-grams to generate
	 * @return A list of n-gram arrays
	 */
	private List<String[]> getNGrams(String[] tokens, int n)
	{
		List<String[]> ngrams = new ArrayList<>();
		for (int i = 0; i <= tokens.length - n; i++)
		{
			String[] ngram = Arrays.copyOfRange(tokens, i, i + n);
			ngrams.add(ngram);
		}
		return ngrams;
	}

	/**
	 * Counts the overlap between two collections of n-grams based on the configured counting mode.
	 *
	 * @param candidateNgrams
	 * 	List of candidate n-grams as strings
	 * @param referenceNgrams
	 * 	List of reference n-grams as strings
	 * @return Number of matching n-grams
	 */
	private int calculateNgramOverlap(List<String[]> candidateNgrams, List<String[]> referenceNgrams)
	{
		List<String> candidateList = new ArrayList<>(ngramsToStrings(candidateNgrams));
		List<String> referenceList = new ArrayList<>(ngramsToStrings(referenceNgrams));

		int overlap = 0;
		List<String> remainingRef = new ArrayList<>(referenceList);

		for (String item : candidateList)
		{
			int index = remainingRef.indexOf(item);
			if (index != -1)
			{
				overlap++;
				remainingRef.remove(index);
			}
		}
		return overlap;
	}

	/**
	 * Converts an array of tokens representing an n-gram into a string representation. Used for comparison and counting of n-grams.
	 *
	 * @param ngrams
	 * 	List of tokens forming an n-gram
	 * @return String representation of the n-gram
	 */
	private List<String> ngramsToStrings(List<String[]> ngrams)
	{
		List<String> result = new ArrayList<>();
		for (String[] ngram : ngrams)
		{
			result.add(String.join(" ", ngram));
		}
		return result;
	}

	/**
	 * Calculates the F1 score based on the provided precision and recall values.
	 *
	 * @param precision
	 * 	The precision value
	 * @param recall
	 * 	The recall value
	 * @return Returns the F1 score
	 */
	private double getScore(double precision, double recall)
	{
		switch (measureType)
		{
			case PRECISION:
				return precision;
			case RECALL:
				return recall;
			case F1SCORE:
				if (precision + recall < EPSILON)
				{
					return 0.0;
				}
				return 2.0 * (precision * recall) / (precision + recall + EPSILON);
			default:
				throw new IllegalStateException("Unsupported measure type: " + measureType);
		}
	}
}