package dev.rage4j.evaluation.axcel;

/**
 * Represents a single fact evaluation entry produced by the Axcel evaluator
 * prompt. Each entry contains the fact index, title, supporting text snippets,
 * and the rating assigned by the LLM (if present).
 */
public record AxcelFactEvaluation(
	int index,
	String title,
	String derivedText,
	String sourceText,
	String verification,
	Integer rating)
{
	public boolean hasRating()
	{
		return rating != null;
	}
}
