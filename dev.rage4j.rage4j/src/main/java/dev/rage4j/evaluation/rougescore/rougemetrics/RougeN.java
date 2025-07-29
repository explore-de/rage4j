package dev.rage4j.evaluation.rougescore.rougemetrics;

import dev.rage4j.evaluation.rougescore.model.Measurement;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static dev.rage4j.util.NGramUtils.getNGrams;

public class RougeN
{
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
	public static Measurement calculateRougeN(String[] candidate, String[] reference, int n)
	{
		if (candidate.length < n || reference.length < n)
		{
			return new Measurement(0, 0);
		}

		List<String[]> candidateNgrams = getNGrams(candidate, n);
		List<String[]> referenceNgrams = getNGrams(reference, n);

		int overlap = calculateNgramOverlap(candidateNgrams, referenceNgrams);

		double precision = (double)overlap / candidateNgrams.size();
		double recall = (double)overlap / referenceNgrams.size();

		return new Measurement(precision, recall);
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
	private static int calculateNgramOverlap(List<String[]> candidateNgrams, List<String[]> referenceNgrams)
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
	private static List<String> ngramsToStrings(List<String[]> ngrams)
	{
		return ngrams.stream()
			.map(ngram -> String.join(" ", ngram))
			.collect(Collectors.toList());
	}
}
