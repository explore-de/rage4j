package dev.rage4j.model;

import java.util.List;

/**
 * The outcome of matching expected tool calls against the ones a language model
 * actually performed.
 *
 * @param matchedExpectedCalls
 *            How many of the expected calls were satisfied.
 * @param unmatchedActualCalls
 *            The actual calls that no expectation covers.
 */
public record ToolCallMatchResult(int matchedExpectedCalls, List<ToolCall> unmatchedActualCalls)
{
	public ToolCallMatchResult
	{
		unmatchedActualCalls = List.copyOf(unmatchedActualCalls);
	}
}
