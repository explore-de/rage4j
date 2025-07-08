package dev.rage4j.evaluation.rougescore.rougemetrics;

import dev.rage4j.evaluation.rougescore.model.Measurement;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RougeL
{
	public static Measurement calculateRougeL(String[] candidate, String[] reference)
	{
		int[][] lcsTable = computeLCSTable(candidate, reference);
		int lcsLength = lcsTable[candidate.length][reference.length];
		int m = reference.length;
		int n = candidate.length;

		return getMeasurement(lcsLength, m, n);
	}

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

	@NotNull
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

	private static int[][] computeLCSTable(String[] a, String[] b)
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

	private static int computeUnionLCS(List<String[]> referenceSentences,
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

	private static boolean isSentenceBoundary(String token)
	{
		return token.equals("\n");
	}

	public static String[] removeString(String[] array, String toRemove)
	{
		if (array == null || toRemove == null) return array;

		List<String> result = new ArrayList<>();
		for (String s : array)
		{
			if (!toRemove.equals(s))
			{
				result.add(s);
			}
		}
		return result.toArray(new String[0]);
	}
}
