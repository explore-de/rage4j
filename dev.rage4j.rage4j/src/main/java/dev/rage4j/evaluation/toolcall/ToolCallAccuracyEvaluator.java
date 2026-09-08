package dev.rage4j.evaluation.toolcall;

import dev.rage4j.evaluation.Evaluation;
import dev.rage4j.evaluation.Evaluator;
import dev.rage4j.model.Sample;
import dev.rage4j.model.ToolCall;
import dev.rage4j.model.ToolCallMatchResult;
import dev.rage4j.model.ToolCallMatching;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * The {@code ToolCallAccuracyEvaluator} class measures how many of the tool
 * calls a test expects were actually performed by the language model. It
 * compares the expected calls of a {@link Sample} against the calls the model
 * reported, without involving a language model itself.
 * <p>
 * An expected call matches an actual one when the tool names are equal and
 * every expected argument is present with an equivalent value. Expected
 * arguments are therefore a subset of the actual ones, and an expectation
 * without arguments matches any call of the same name.
 * <p>
 * The result is the fraction of expected calls that were matched, between 0 and
 * 1. Each actual call satisfies at most one expectation, so a model that calls
 * a tool once cannot satisfy two expectations of it. Additional calls the test
 * did not expect do not lower the score.
 */
public class ToolCallAccuracyEvaluator implements Evaluator
{
	private static final String METRIC_NAME = "Tool Call Accuracy";
	private static final Logger LOG = LoggerFactory.getLogger(ToolCallAccuracyEvaluator.class);

	/**
	 * Evaluates how many of the expected tool calls the model performed.
	 *
	 * @param sample
	 *            The sample containing both the expected and the actual tool
	 *            calls.
	 * @return An Evaluation object containing the tool call accuracy.
	 * @throws IllegalArgumentException
	 *             if the sample carries no expected tool calls, or if no actual
	 *             tool calls were recorded on it.
	 */
	@Override
	public Evaluation evaluate(Sample sample)
	{
		if (!sample.hasExpectedToolCalls())
		{
			throw new IllegalArgumentException("Sample must have expected tool calls for Tool Call Accuracy evaluation");
		}
		if (!sample.hasToolCalls())
		{
			throw new IllegalArgumentException("Sample must have recorded tool calls for Tool Call Accuracy evaluation");
		}

		List<ToolCall> expectedToolCalls = sample.getExpectedToolCalls();
		List<ToolCall> actualToolCalls = sample.getToolCalls();
		LOG.info("Evaluating new sample");
		LOG.info("Expected tool calls: {}", expectedToolCalls);
		LOG.info("Actual tool calls: {}", actualToolCalls);

		ToolCallMatchResult matchResult = ToolCallMatching.match(expectedToolCalls, actualToolCalls);
		double score = (double)matchResult.matchedExpectedCalls() / expectedToolCalls.size();
		LOG.info("Tool Call Accuracy: {}", score);
		return new Evaluation(METRIC_NAME, score);
	}
}
