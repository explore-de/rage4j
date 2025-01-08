package dev.rage4j.evaluation.answercorrectness;

import dev.rage4j.LoggingTestWatcher;
import dev.rage4j.evaluation.Evaluation;
import dev.rage4j.evaluation.model.ArrayResponse;
import dev.rage4j.model.Sample;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

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

	@Test
	void testEvaluateWithTrueAndFalsePositives()
	{
		when(mockBot.extractTruePositiveClaims(GROUND_TRUTH, ANSWER))
			.thenReturn(new ArrayResponse(new String[1]));
		when(mockBot.extractFalsePositiveClaims(GROUND_TRUTH, ANSWER))
			.thenReturn(new ArrayResponse(new String[1]));
		when(mockBot.extractFalseNegativeClaims(GROUND_TRUTH, ANSWER))
			.thenReturn(new ArrayResponse(new String[0]));

		Evaluation result = evaluator.evaluate(sample);

		assertEquals("Answer correctness", result.getName());
		assertEquals(0.6667, result.getValue(), 0.001);
	}

	@Test
	void testEvaluateWithNoTrueFalsePositivesOrNegatives()
	{
		when(mockBot.extractTruePositiveClaims(GROUND_TRUTH, ANSWER))
			.thenReturn(new ArrayResponse(new String[0]));
		when(mockBot.extractFalsePositiveClaims(GROUND_TRUTH, ANSWER))
			.thenReturn(new ArrayResponse(new String[0]));
		when(mockBot.extractFalseNegativeClaims(GROUND_TRUTH, ANSWER))
			.thenReturn(new ArrayResponse(new String[0]));

		Evaluation result = evaluator.evaluate(sample);

		assertEquals("Answer correctness", result.getName());
		assertEquals(0.0, result.getValue(), 0.001);
	}

	@Test
	void testEvaluateWithOnlyTruePositives()
	{
		when(mockBot.extractTruePositiveClaims(GROUND_TRUTH, ANSWER))
			.thenReturn(new ArrayResponse(new String[1]));
		when(mockBot.extractFalsePositiveClaims(GROUND_TRUTH, ANSWER))
			.thenReturn(new ArrayResponse(new String[0]));
		when(mockBot.extractFalseNegativeClaims(GROUND_TRUTH, ANSWER))
			.thenReturn(new ArrayResponse(new String[0]));

		Evaluation result = evaluator.evaluate(sample);

		assertEquals("Answer correctness", result.getName());
		assertEquals(1.0, result.getValue(), 0.001);
	}

	@Test
	void testEvaluateAnswerCorrectnessNullAnswer()
	{
		Sample nullAnswerSample = Sample.builder()
			.withGroundTruth(GROUND_TRUTH)
			.withAnswer(null)
			.build();

		IllegalStateException exception = assertThrows(IllegalStateException.class, () -> evaluator.evaluate(nullAnswerSample));
		assertEquals("Attribute not found: answer", exception.getMessage());
	}

	@Test
	void testEvaluateAnswerCorrectnessNullGroundTruth()
	{
		Sample nullGroundTruthSample = Sample.builder()
			.withGroundTruth(null)
			.withAnswer(ANSWER)
			.build();

		IllegalStateException exception = assertThrows(IllegalStateException.class, () -> evaluator.evaluate(nullGroundTruthSample));
		assertEquals("Attribute not found: groundTruth", exception.getMessage());
	}
}
