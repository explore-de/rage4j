package dev.rage4j.evaluation.rougescore.rougemetrics;

import dev.rage4j.evaluation.rougescore.model.Measurement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static dev.rage4j.evaluation.rougescore.util.LCS.computeLCSTable;
import static dev.rage4j.evaluation.rougescore.util.LCS.computeUnionLCS;

public class RougeL
{
	/**
	 * Calculates ROUGE-L precision and recall scores based on the longest common subsequence between the candidate and reference token arrays.
	 *
	 * @param candidate
	 * 	An array of tokens from the candidate text.
	 * @param reference
	 * 	An array of tokens from the reference text.
	 * @return A {@link Measurement} object containing precision and recall scores.
	 */
	public static Measurement calculateRougeL(String[] candidate, String[] reference)
	{
		int[][] lcsTable = computeLCSTable(candidate, reference);
		int lcsLength = lcsTable[candidate.length][reference.length];
		int m = reference.length;
		int n = candidate.length;

		return getMeasurement(lcsLength, m, n);
	}

	/**
	 * Calculates ROUGE-LSum by computing the union of LCS lengths over sentences in the candidate and reference texts. Sentence boundaries are identified by newline tokens ("\n").
	 *
	 * @param candidateTokens
	 * 	Token array from the candidate text, including "\n" for sentence breaks.
	 * @param referenceTokens
	 * 	Token array from the reference text, including "\n" for sentence breaks.
	 * @return A {@link Measurement} object containing precision and recall scores.
	 */
	public static Measurement calculateRougeLsum(String[] candidateTokens, String[] referenceTokens)
	{
		List<String[]> candidateSentences = splitIntoSentences(candidateTokens);
		List<String[]> referenceSentences = splitIntoSentences(referenceTokens);

		candidateTokens = removeString(candidateTokens, "\n");
		referenceTokens = removeString(referenceTokens, "\n");

		int totalRefTokens = referenceTokens.length;
		int totalCandTokens = candidateTokens.length;

		int unionLCS = computeUnionLCS(referenceSentences, candidateSentences);

		return getMeasurement(unionLCS, totalRefTokens, totalCandTokens);
	}

	/**
	 * Computes precision and recall based on LCS length and the sizes of the reference and candidate texts.
	 *
	 * @param lcsLength
	 * 	Length of the longest common subsequence.
	 * @param m
	 * 	Number of tokens in the reference text.
	 * @param n
	 * 	Number of tokens in the candidate text.
	 * @return A {@link Measurement} object containing precision and recall.
	 */
	private static Measurement getMeasurement(double lcsLength, int m, int n)
	{
		if (m == 0 || n == 0)
		{
			return new Measurement(0.0, 0.0);
		}

		double recall = lcsLength / m;
		double precision = lcsLength / n;

		return new Measurement(precision, recall);
	}

	/**
	 * Splits an array of tokens into individual sentences using "\n" as the sentence boundary marker.
	 *
	 * @param tokens
	 * 	The full token array, including "\n" as sentence breaks.
	 * @return A list of string arrays, each representing a sentence.
	 */
	private static List<String[]> splitIntoSentences(String[] tokens)
	{
		List<String[]> sentences = new ArrayList<>();
		List<String> current = new ArrayList<>();

		for (String token : tokens)
		{
			if (isSentenceBoundary(token))
			{
				if (!current.isEmpty())
				{
					sentences.add(current.toArray(new String[0]));
					current.clear();
				}
			}
			else
			{
				current.add(token);
			}
		}

		if (!current.isEmpty())
		{
			sentences.add(current.toArray(new String[0]));
		}

		return sentences;
	}

	/**
	 * Determines whether a given token marks the end of a sentence. Currently, uses "\n" as the sentence boundary marker.
	 *
	 * @param token
	 * 	The token to check.
	 * @return {@code true} if the token is a sentence boundary; otherwise {@code false}.
	 */
	private static boolean isSentenceBoundary(String token)
	{
		return token.equals("\n");
	}

	private static String[] removeString(String[] array, String toRemove) {
		if (array == null || toRemove == null) {
			return array;
		}
		return Arrays.stream(array)
			.filter(s -> !toRemove.equals(s))
			.toArray(String[]::new);
	}
}
