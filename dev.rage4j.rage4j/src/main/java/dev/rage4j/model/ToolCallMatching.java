package dev.rage4j.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * The {@code ToolCallMatching} class pairs expected tool calls with the ones a
 * language model actually performed, so that each actual call satisfies at most
 * one expectation.
 * <p>
 * The pairing is a maximum bipartite matching, computed with Kuhn's algorithm.
 * Taking the first match for each expectation in turn would under-report: an
 * expectation without arguments matches any call of its name and would consume
 * the very call that a more specific expectation of the same tool needs, even
 * though a pairing satisfying both exists.
 */
public final class ToolCallMatching
{
	private static final int UNMATCHED = -1;

	private ToolCallMatching()
	{
	}

	/**
	 * Pairs the expected tool calls with the actual ones.
	 *
	 * @param expectedToolCalls
	 *            The tool calls a test expects.
	 * @param actualToolCalls
	 *            The tool calls the language model performed.
	 * @return How many expectations were satisfied, and which actual calls no
	 *         expectation covers.
	 */
	public static ToolCallMatchResult match(List<ToolCall> expectedToolCalls, List<ToolCall> actualToolCalls)
	{
		Objects.requireNonNull(expectedToolCalls, "expectedToolCalls");
		Objects.requireNonNull(actualToolCalls, "actualToolCalls");

		int[] expectationForActualCall = new int[actualToolCalls.size()];
		Arrays.fill(expectationForActualCall, UNMATCHED);

		int matchedExpectedCalls = 0;
		for (int expectedIndex = 0; expectedIndex < expectedToolCalls.size(); expectedIndex++)
		{
			if (assign(expectedIndex, expectedToolCalls, actualToolCalls, expectationForActualCall, new boolean[actualToolCalls.size()]))
			{
				matchedExpectedCalls++;
			}
		}

		List<ToolCall> unmatchedActualCalls = new ArrayList<>();
		for (int actualIndex = 0; actualIndex < expectationForActualCall.length; actualIndex++)
		{
			if (expectationForActualCall[actualIndex] == UNMATCHED)
			{
				unmatchedActualCalls.add(actualToolCalls.get(actualIndex));
			}
		}
		return new ToolCallMatchResult(matchedExpectedCalls, unmatchedActualCalls);
	}

	private static boolean assign(int expectedIndex, List<ToolCall> expectedToolCalls, List<ToolCall> actualToolCalls, int[] expectationForActualCall, boolean[] visitedActualCalls)
	{
		for (int actualIndex = 0; actualIndex < actualToolCalls.size(); actualIndex++)
		{
			if (visitedActualCalls[actualIndex] || !expectedToolCalls.get(expectedIndex).matches(actualToolCalls.get(actualIndex)))
			{
				continue;
			}
			visitedActualCalls[actualIndex] = true;
			if (expectationForActualCall[actualIndex] == UNMATCHED
				|| assign(expectationForActualCall[actualIndex], expectedToolCalls, actualToolCalls, expectationForActualCall, visitedActualCalls))
			{
				expectationForActualCall[actualIndex] = expectedIndex;
				return true;
			}
		}
		return false;
	}
}
