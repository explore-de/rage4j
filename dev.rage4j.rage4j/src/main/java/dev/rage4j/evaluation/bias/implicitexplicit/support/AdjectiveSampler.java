package dev.rage4j.evaluation.bias.implicitexplicit.support;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class AdjectiveSampler
{
	private final Set<String> positiveAdjectives;
	private final Set<String> negativeAdjectives;
	private final Set<String> neutralAdjectives;

	public AdjectiveSampler(List<String> positiveAdjectives, List<String> negativeAdjectives, List<String> neutralAdjectives)
	{
		this.positiveAdjectives = convertToSet(positiveAdjectives);
		this.negativeAdjectives = convertToSet(negativeAdjectives);
		this.neutralAdjectives = convertToSet(neutralAdjectives);
	}

	public Integer convertAdjectives(String response)
	{
		// split answer into separated adjectives
		String[] entries = response.split(",");
		int positive = 0, negative = 0, neutral = 0;

		// increment if there's a hit
		for (String entry : entries)
		{
			String cleaned = normalizeEntry(entry);
			if (cleaned.isEmpty())
			{
				continue;
			}
			if (positiveAdjectives.contains(cleaned))
			{
				positive++;
			}
			else if (negativeAdjectives.contains(cleaned))
			{
				negative++;
			}
			else if (neutralAdjectives.contains(cleaned))
			{
				neutral++;
			}
		}

		// validate hits
		int total = positive + negative + neutral;
		if (total == 0)
		{
			throw new IllegalStateException("Implicit bias evaluation didn't find any adjective matches");
		}
		return positive - negative;
	}

	private static Set<String> convertToSet(java.util.List<String> adjectives)
	{
		return adjectives.stream()
			.map(AdjectiveSampler::normalizeEntry)
			.collect(Collectors.toSet());
	}

	private static String normalizeEntry(String s)
	{
		return s.toLowerCase()
			.trim()
			.replaceAll("[^a-z\\s-]", "")
			.replaceAll("\\s+", " ")
			.trim();
	}
}
