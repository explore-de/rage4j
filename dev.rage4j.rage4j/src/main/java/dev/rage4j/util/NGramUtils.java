package dev.rage4j.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NGramUtils
{
	/**
	 * Generates n-grams from an array of tokens.
	 *
	 * @param tokens
	 *            The input array of tokens
	 * @param n
	 *            The size of n-grams to generate
	 * @return A list of n-gram string arrays
	 */
	public static List<String[]> getNGrams(String[] tokens, int n)
	{
		if (tokens == null || tokens.length == 0)
		{
			throw new IllegalArgumentException("Tokens array cannot be null or empty");
		}
		if (n <= 0)
		{
			throw new IllegalArgumentException("N must be positive");
		}
		if (tokens.length < n)
		{
			return new ArrayList<>();
		}

		List<String[]> ngrams = new ArrayList<>();
		for (int i = 0; i <= tokens.length - n; i++)
		{
			ngrams.add(Arrays.copyOfRange(tokens, i, i + n));
		}
		return ngrams;
	}

	/**
	 * Creates a frequency map of n-grams from an array of tokens.
	 *
	 * @param tokens
	 *            The input array of tokens
	 * @param n
	 *            The size of n-grams to generate
	 * @return A map of n-gram strings to their frequency counts
	 */
	public static Map<String, Integer> getNGramCounts(String[] tokens, int n)
	{
		return getNGrams(tokens, n).stream()
			.map(ngram -> String.join(" ", ngram))
			.collect(Collectors.groupingBy(
				s -> s,
				HashMap::new,
				Collectors.collectingAndThen(Collectors.counting(), Long::intValue)));
	}
}
