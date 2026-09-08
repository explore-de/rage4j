package dev.rage4j.evaluation.toolcall;

import dev.rage4j.LoggingTestWatcher;
import dev.rage4j.evaluation.Evaluation;
import dev.rage4j.model.Sample;
import dev.rage4j.model.ToolCall;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(LoggingTestWatcher.class)
class ToolCallAccuracyEvaluatorTest
{
	private ToolCallAccuracyEvaluator evaluator;

	@BeforeEach
	void setUp()
	{
		evaluator = new ToolCallAccuracyEvaluator();
	}

	@Test
	void testExactMatchScoresOne()
	{
		Sample sample = sample(
			List.of(ToolCall.of("getWeather").withArgument("city", "Berlin")),
			List.of(ToolCall.of("getWeather").withArgument("city", "Berlin")));

		Evaluation result = evaluator.evaluate(sample);

		assertEquals("Tool Call Accuracy", result.getName());
		assertEquals(1.0, result.getValue(), 0.001);
	}

	@Test
	void testHalfOfTheExpectedCallsScoresOneHalf()
	{
		Sample sample = sample(
			List.of(ToolCall.of("findCustomer"), ToolCall.of("cancelBooking")),
			List.of(ToolCall.of("findCustomer")));

		assertEquals(0.5, evaluator.evaluate(sample).getValue(), 0.001);
	}

	@Test
	void testDifferentArgumentValueScoresZero()
	{
		Sample sample = sample(
			List.of(ToolCall.of("getWeather").withArgument("city", "Berlin")),
			List.of(ToolCall.of("getWeather").withArgument("city", "Hamburg")));

		assertEquals(0.0, evaluator.evaluate(sample).getValue(), 0.001);
	}

	@Test
	void testExpectedWithoutArgumentsMatchesOnNameAlone()
	{
		Sample sample = sample(
			List.of(ToolCall.of("getWeather")),
			List.of(ToolCall.of("getWeather").withArgument("city", "Berlin")));

		assertEquals(1.0, evaluator.evaluate(sample).getValue(), 0.001);
	}

	@Test
	void testExpectedArgumentsMayBeASubsetOfTheActualOnes()
	{
		Sample sample = sample(
			List.of(ToolCall.of("getWeather").withArgument("city", "Berlin")),
			List.of(ToolCall.of("getWeather").withArguments(Map.of("city", "Berlin", "day", "tomorrow"))));

		assertEquals(1.0, evaluator.evaluate(sample).getValue(), 0.001);
	}

	@Test
	void testEquivalentNumbersMatchAcrossTypes()
	{
		Sample sample = sample(
			List.of(ToolCall.of("listBookings").withArgument("limit", 3)),
			List.of(ToolCall.of("listBookings").withArgument("limit", 3L)));

		assertEquals(1.0, evaluator.evaluate(sample).getValue(), 0.001);
	}

	@Test
	void testUnexpectedAdditionalCallsDoNotLowerTheScore()
	{
		Sample sample = sample(
			List.of(ToolCall.of("getWeather")),
			List.of(ToolCall.of("getWeather"), ToolCall.of("sendMail")));

		assertEquals(1.0, evaluator.evaluate(sample).getValue(), 0.001);
	}

	@Test
	void testEachExpectedCallIsMatchedAtMostOnce()
	{
		Sample sample = sample(
			List.of(ToolCall.of("getWeather"), ToolCall.of("getWeather")),
			List.of(ToolCall.of("getWeather")));

		assertEquals(0.5, evaluator.evaluate(sample).getValue(), 0.001);
	}

	@Test
	void testUnconstrainedExpectationDoesNotConsumeTheCallASpecificOneNeeds()
	{
		Sample sample = sample(
			List.of(ToolCall.of("getWeather"), ToolCall.of("getWeather").withArgument("city", "Berlin")),
			List.of(ToolCall.of("getWeather").withArgument("city", "Berlin"), ToolCall.of("getWeather").withArgument("city", "Hamburg")));

		assertEquals(1.0, evaluator.evaluate(sample).getValue(), 0.001);
	}

	@Test
	void testOverlappingExpectationsAreMatchedOptimally()
	{
		Sample sample = sample(
			List.of(ToolCall.of("search").withArgument("query", "x"), ToolCall.of("search").withArguments(Map.of("query", "x", "limit", 10))),
			List.of(ToolCall.of("search").withArguments(Map.of("query", "x", "limit", 10)), ToolCall.of("search").withArguments(Map.of("query", "x", "limit", 5))));

		assertEquals(1.0, evaluator.evaluate(sample).getValue(), 0.001);
	}

	@Test
	void testNoActualToolCallsScoresZero()
	{
		Sample sample = sample(List.of(ToolCall.of("getWeather")), List.of());

		assertEquals(0.0, evaluator.evaluate(sample).getValue(), 0.001);
	}

	@Test
	void testMissingExpectedToolCallsThrowsException()
	{
		Sample sample = Sample.builder()
			.withToolCalls(List.of(ToolCall.of("getWeather")))
			.build();

		assertThrows(IllegalArgumentException.class, () -> evaluator.evaluate(sample));
	}

	@Test
	void testMissingActualToolCallsThrowsException()
	{
		Sample sample = Sample.builder()
			.withExpectedToolCalls(List.of(ToolCall.of("getWeather")))
			.build();

		assertThrows(IllegalArgumentException.class, () -> evaluator.evaluate(sample));
	}

	private Sample sample(List<ToolCall> expected, List<ToolCall> actual)
	{
		return Sample.builder()
			.withExpectedToolCalls(expected)
			.withToolCalls(actual)
			.build();
	}
}
