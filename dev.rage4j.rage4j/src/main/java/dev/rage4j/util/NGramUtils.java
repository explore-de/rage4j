package dev.rage4j.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
	public static List<String[]> getNGrams(String[] tokens, int n) {
		Objects.requireNonNull(tokens, "Tokens array cannot be null");
		if (n <= 0) {
			throw new IllegalArgumentException("n must be positive");
		}
		if (tokens.length < n) {
			return Collections.emptyList();
		}

		return IntStream
			.rangeClosed(0, tokens.length - n)
			.mapToObj(i -> Arrays.copyOfRange(tokens, i, i + n))
			.collect(Collectors.toList());
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
