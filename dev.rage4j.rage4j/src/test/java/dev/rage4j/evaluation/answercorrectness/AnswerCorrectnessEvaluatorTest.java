package dev.rage4j.evaluation.answercorrectness;

import dev.rage4j.LoggingTestWatcher;
import dev.rage4j.evaluation.Evaluation;
import dev.rage4j.evaluation.model.ArrayResponse;
import dev.rage4j.model.Sample;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(LoggingTestWatcher.class)
class AnswerCorrectnessEvaluatorTest
{
	private static final String GROUND_TRUTH = "Paris is the capital of France.";
	private static final String ANSWER = "Paris is the capital of France and the largest city in France.";

	private AnswerCorrectnessEvaluator evaluator;
	private AnswerCorrectnessBot mockBot;
	private Sample sample;

	@BeforeEach
	void setUp()
	{
		mockBot = mock(AnswerCorrectnessBot.class);
		evaluator = new AnswerCorrectnessEvaluator(mockBot);

		sample = Sample.builder()
			.withGroundTruth(GROUND_TRUTH)
			.withAnswer(ANSWER)
			.build();
	}

	static Stream<Arguments> evaluateCorrectnessTestCases()
	{
		return Stream.of(
			Arguments.of(1, 1, 0, 0.6667, "true and false positives"),
			Arguments.of(0, 0, 0, 0.0, "no true/false positives or negatives"),
			Arguments.of(1, 0, 0, 1.0, "only true positives")
		);
	}

	@ParameterizedTest(name = "evaluates correctly with {4}")
	@MethodSource("evaluateCorrectnessTestCases")
	void testEvaluateCorrectness(int truePositives, int falsePositives, int falseNegatives,
		double expectedScore, String scenario)
	{
		when(mockBot.extractTruePositiveClaims(GROUND_TRUTH, ANSWER))
			.thenReturn(new ArrayResponse(new String[truePositives]));
		when(mockBot.extractFalsePositiveClaims(GROUND_TRUTH, ANSWER))
			.thenReturn(new ArrayResponse(new String[falsePositives]));
		when(mockBot.extractFalseNegativeClaims(GROUND_TRUTH, ANSWER))
			.thenReturn(new ArrayResponse(new String[falseNegatives]));

		Evaluation result = evaluator.evaluate(sample);

		assertEquals("Answer correctness", result.getName());
		assertEquals(expectedScore, result.getValue(), 0.001);
	}

	@Test
	void testEvaluateAnswerCorrectnessNullAnswer()
	{
		Sample nullAnswerSample = Sample.builder()
			.withGroundTruth(GROUND_TRUTH)
			.withAnswer(null)
			.build();

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> evaluator.evaluate(nullAnswerSample));
		assertEquals("Sample must have an answer for Answer Correctness evaluation", exception.getMessage());
	}

	@Test
	void testEvaluateAnswerCorrectnessNullGroundTruth()
	{
		Sample nullGroundTruthSample = Sample.builder()
			.withGroundTruth(null)
			.withAnswer(ANSWER)
			.build();

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> evaluator.evaluate(nullGroundTruthSample));
		assertEquals("Sample must have a ground truth for Answer Correctness evaluation", exception.getMessage());
	}
}
