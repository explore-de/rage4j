package dev.rage4j.evaluation.bleuscore;

import dev.rage4j.LoggingTestWatcher;
import dev.rage4j.evaluation.Evaluation;
import dev.rage4j.model.Sample;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(LoggingTestWatcher.class)
class BleuScoreEvaluatorTest
{
	private static final String GROUND_TRUTH = "The Eiffel Tower is located in Paris.";
	private static final String ANSWER = "The Eiffel Tower is located in Paris.";

	private BleuScoreEvaluator evaluator;

	@BeforeEach
	void setUp()
	{
		evaluator = new BleuScoreEvaluator();
	}

	@Test
	void testEvaluateHighScore()
	{
		Sample sample = Sample.builder()
			.withGroundTruth(GROUND_TRUTH)
			.withAnswer(ANSWER)
			.build();

		Evaluation result = evaluator.evaluate(sample);
		assertEquals(1.0, result.getValue(), 0.001);
	}

	@Test
	void testEvaluateLowScore()
	{
		double lowScore = 0.2;

		Sample sample = Sample.builder()
			.withGroundTruth("The cat sat on the mat.")
			.withAnswer("Dogs run in the park.")
			.build();

		Evaluation result = evaluator.evaluate(sample);

		assertEquals("BLEU score", result.getName());
		assertTrue(result.getValue() < lowScore,
			"Score " + result.getValue() + " should be less than 0.3");
	}

	@Test
	void testEvaluateZeroScore()
	{
		Sample sample = Sample.builder()
			.withGroundTruth(GROUND_TRUTH)
			.withAnswer("")
			.build();

		Evaluation result = evaluator.evaluate(sample);

		assertEquals("BLEU score", result.getName());
		assertEquals(0.0, result.getValue(), 0.01);
	}

	@Test
	void testEvaluateNullAnswerThrowsException()
	{
		Sample sample = Sample.builder()
			.withGroundTruth(GROUND_TRUTH)
			.withAnswer(null)
			.build();

		IllegalStateException exception = assertThrows(IllegalStateException.class, () -> evaluator.evaluate(sample));
		assertEquals("Attribute not found: answer", exception.getMessage());
	}

	@Test
	void testEvaluateNullGroundTruthThrowsException()
	{
		Sample sample = Sample.builder()
			.withAnswer(ANSWER)
			.withGroundTruth(null)
			.build();

		IllegalStateException exception = assertThrows(IllegalStateException.class, () -> evaluator.evaluate(sample));
		assertEquals("Attribute not found: groundTruth", exception.getMessage());
	}
}