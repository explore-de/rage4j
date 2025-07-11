package dev.rage4j.evaluation.rougescore.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LCS
{
	/**
	 * Computes the dynamic programming table for the Longest Common Subsequence (LCS) between two token sequences.
	 *
	 * @param a
	 * 	First sequence of tokens.
	 * @param b
	 * 	Second sequence of tokens.
	 * @return A 2D table where cell [i][j] represents the length of the LCS between the first i tokens of {@code a} and the first j tokens of {@code b}.
	 */
	public static int[][] computeLCSTable(String[] a, String[] b)
	{
		int m = a.length;
		int n = b.length;
		int[][] dp = new int[m + 1][n + 1];

		for (int i = 1; i <= m; i++)
		{
			String wa = a[i - 1];
			for (int j = 1; j <= n; j++)
			{
				String wb = b[j - 1];
				if (wa.equals(wb))
				{
					dp[i][j] = dp[i - 1][j - 1] + 1;
				}
				else
				{
					dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
				}
			}
		}

		return dp;
	}

	/**
	 * Computes the union of LCS matches across all sentence pairs between reference and candidate texts, ensuring no duplicate matches. Used for ROUGE-LSum.
	 *
	 * @param referenceSentences
	 * 	List of reference sentences, each as a token array.
	 * @param candidateSentences
	 * 	List of candidate sentences, each as a token array.
	 * @return Total number of non-overlapping LCS token matches across all pairs.
	 */
	public static int computeUnionLCS(List<String[]> referenceSentences,
		List<String[]> candidateSentences)
	{

		int[] refOffsets = buildOffsets(referenceSentences);
		int[] candOffsets = buildOffsets(candidateSentences);

		Set<Integer> usedRef = new HashSet<>();
		Set<Integer> usedCand = new HashSet<>();

		int union = 0;

		for (int r = 0; r < referenceSentences.size(); r++)
		{
			String[] ref = referenceSentences.get(r);
			for (int c = 0; c < candidateSentences.size(); c++)
			{
				union += countFreshMatches(ref, candidateSentences.get(c),
					refOffsets[r], candOffsets[c],
					usedRef, usedCand);
			}
		}
		return union;
	}

	/**
	 * Reconstructs the LCS match positions from a filled LCS table for two token sequences.
	 *
	 * @param a
	 * 	First sequence of tokens.
	 * @param b
	 * 	Second sequence of tokens.
	 * @param dp
	 * 	Precomputed LCS table (from {@link #computeLCSTable}).
	 * @return A list of int pairs [i, j] indicating matched token positions in {@code a} and {@code b}.
	 */
	private static List<int[]> backtrackLCS(String[] a, String[] b, int[][] dp)
	{
		List<int[]> positions = new ArrayList<>();
		int i = a.length;
		int j = b.length;

		while (i > 0 && j > 0)
		{
			if (a[i - 1].equals(b[j - 1]))
			{
				positions.add(new int[] { i - 1, j - 1 });
				i--;
				j--;
			}
			else if (dp[i - 1][j] >= dp[i][j - 1])
			{
				i--;
			}
			else
			{
				j--;
			}
		}

		return positions;
	}

	/**
	 * Builds an array of token offsets for each sentence in a document, allowing mapping of local sentence token indices to global document indices.
	 *
	 * @param sentences
	 * 	List of token arrays representing individual sentences.
	 * @return Array of global token offsets for each sentence.
	 */
	private static int[] buildOffsets(List<String[]> sentences)
	{
		int[] offsets = new int[sentences.size()];
		int cursor = 0;
		for (int i = 0; i < sentences.size(); i++)
		{
			offsets[i] = cursor;
			cursor += sentences.get(i).length;
		}
		return offsets;
	}

	/**
	 * Counts the number of unique LCS token matches between a reference and candidate sentence pair, while ensuring tokens are not reused (clipped matching).
	 *
	 * @param refSent
	 * 	Token array of the reference sentence.
	 * @param candSent
	 * 	Token array of the candidate sentence.
	 * @param refOffset
	 * 	Global token offset for the reference sentence.
	 * @param candOffset
	 * 	Global token offset for the candidate sentence.
	 * @param usedRef
	 * 	Set of already matched reference token indices (global).
	 * @param usedCand
	 * 	Set of already matched candidate token indices (global).
	 * @return Number of fresh (non-overlapping) LCS matches.
	 */
	private static int countFreshMatches(String[] refSent, String[] candSent,
		int refOffset, int candOffset,
		Set<Integer> usedRef, Set<Integer> usedCand)
	{
		int[][] dp = computeLCSTable(refSent, candSent);
		List<int[]> matches = backtrackLCS(refSent, candSent, dp);

		int fresh = 0;
		for (int[] pos : matches)
		{
			int globalRef = refOffset + pos[0];
			int globalCand = candOffset + pos[1];

			if (!usedRef.contains(globalRef) && !usedCand.contains(globalCand))
			{
				usedRef.add(globalRef);
				usedCand.add(globalCand);
				fresh++;
			}
		}
		return fresh;
	}
}
