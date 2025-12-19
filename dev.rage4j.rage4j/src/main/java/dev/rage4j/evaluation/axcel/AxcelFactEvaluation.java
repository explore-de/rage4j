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
		return "AxcelFactEvaluation[\n" +
			"  index=" + index + "\n" +
			"  title='" + title + "'\n" +
			"  derivedText='" + (derivedText != null ? derivedText : "null") + "'\n" +
			"  sourceText='" + (sourceText != null ? sourceText : "null") + "'\n" +
			"  verification='" + (verification != null ? verification : "null") + "'\n" +
			"  rating=" + rating + "\n" +
			']';
	}
}
