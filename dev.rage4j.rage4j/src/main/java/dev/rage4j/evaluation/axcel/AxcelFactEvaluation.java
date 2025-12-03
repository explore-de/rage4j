package dev.rage4j.evaluation.axcel;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a single fact evaluation entry produced by the Axcel evaluator prompt. Each entry contains the fact index, title, supporting text snippets, and the rating assigned by the LLM (if present).
 */
public record AxcelFactEvaluation(
	int index,
	String title,
	String derivedText,
	String sourceText,
	String verification,
	Integer rating)
{
	@Override
	public @NotNull String toString()
	{
		return "AxcelFactEvaluation[" +
			"index=" + index +
			", title='" + title + '\'' +
			", derivedText='" + (derivedText != null ? truncate(derivedText) : "null") + '\'' +
			", sourceText='" + (sourceText != null ? truncate(sourceText) : "null") + '\'' +
			", verification='" + (verification != null ? truncate(verification) : "null") + '\'' +
			", rating=" + rating +
			']';
	}

	private static String truncate(String text)
	{
		int maxLength = 200;
		if (text.length() <= maxLength)
		{
			return text;
		}
		return text.substring(0, maxLength) + "...";
	}
}
