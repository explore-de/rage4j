package dev.rage4j.evaluation.rougescore.rougemetrics;

import dev.rage4j.evaluation.rougescore.model.Measurement;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RougeL
{
	public static Measurement computeRougeL(String[] candidate, String[] reference) {
		int[][] lcsTable = computeLCSTable(candidate, reference);
		int lcsLength = lcsTable[candidate.length][reference.length];
		int m = reference.length;
		int n = candidate.length;

		return getMeasurement(lcsLength, m, n);
	}

	public static Measurement computeRougeLsum(String[] candidateTokens, String[] referenceTokens) {
		List<String[]> candidateSentences = splitIntoSentences(candidateTokens);
		List<String[]> referenceSentences = splitIntoSentences(referenceTokens);

		int totalRefTokens = referenceTokens.length;
		int totalCandTokens = candidateTokens.length;

		int unionLCS = computeUnionLCS(referenceSentences, candidateSentences);

		return getMeasurement(unionLCS, totalRefTokens, totalCandTokens);
	}

	@NotNull
	private static Measurement getMeasurement(double lcsLength, int m, int n)
	{
		if (m == 0 || n == 0) {
			return new Measurement(0.0, 0.0);
		}

		double recall = lcsLength / m;
		double precision = lcsLength / n;

		return new Measurement(precision, recall);
	}

	private static int[][] computeLCSTable(String[] a, String[] b) {
		int m = a.length;
		int n = b.length;
		int[][] dp = new int[m + 1][n + 1];

		for (int i = 1; i <= m; i++) {
			String wa = a[i - 1];
			for (int j = 1; j <= n; j++) {
				String wb = b[j - 1];
				if (wa.equals(wb)) {
					dp[i][j] = dp[i - 1][j - 1] + 1;
				} else {
					dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
				}
			}
		}

		return dp;
	}

	private static List<int[]> backtrackLCS(String[] a, String[] b, int[][] dp) {
		List<int[]> positions = new ArrayList<>();
		int i = a.length;
		int j = b.length;

		while (i > 0 && j > 0) {
			if (a[i - 1].equals(b[j - 1])) {
				positions.add(new int[]{i - 1, j - 1});
				i--;
				j--;
			} else if (dp[i - 1][j] >= dp[i][j - 1]) {
				i--;
			} else {
				j--;
			}
		}

		return positions;
	}

	private static int computeUnionLCS(List<String[]> references, List<String[]> candidates) {
		Set<String> matched = new HashSet<>();
		int union = 0;

		for (String[] ref : references) {
			for (String[] cand : candidates) {
				int[][] dp = computeLCSTable(ref, cand);
				List<int[]> positions = backtrackLCS(ref, cand, dp);
				for (int[] pos : positions) {
					String key = pos[0] + "_" + pos[1];
					if (matched.add(key)) {
						union++;
					}
				}
			}
		}

		return union;
	}

	private static List<String[]> splitIntoSentences(String[] tokens) {
		List<String[]> sentences = new ArrayList<>();
		List<String> current = new ArrayList<>();

		for (String token : tokens) {
			if (isSentenceBoundary(token)) {
				String cleaned = token.substring(0, token.length() - 1);
				if (!cleaned.isEmpty()) {
					current.add(cleaned);
				}
				if (!current.isEmpty()) {
					sentences.add(current.toArray(new String[0]));
					current.clear();
				}
			}
			else {
				current.add(token);
			}
		}

		if (!current.isEmpty()) {
			sentences.add(current.toArray(new String[0]));
		}

		return sentences;
	}

	//This only works for our current tokenization method of splitting strings by whitespaces
	private static boolean isSentenceBoundary(String token) {
		return token.endsWith("\n");
	}
}
