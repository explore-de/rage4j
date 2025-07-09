package dev.rage4j.evaluation.rougescore;

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
class RougeScoreEvaluatorTest
{
	private static final String GROUND_TRUTH = "The Eiffel Tower is located in Paris.";
	private static final String ANSWER = "The Eiffel Tower is located in Paris.";

	private RougeScoreEvaluator evaluator;

	@BeforeEach
	void setUp()
	{
		evaluator = new RougeScoreEvaluator();
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
		Sample sample = Sample.builder()
			.withGroundTruth("The cat sat on the mat.")
			.withAnswer("Dogs run in the park.")
			.build();

		Evaluation result = evaluator.evaluate(sample);
		assertTrue(result.getValue() < 0.3);
	}

	@Test
	void testEvaluateRougeTypeOne()
	{
		Sample sample = Sample.builder()
			.withGroundTruth("The quick brown fox jumps over the lazy dog")
			.withAnswer("The quick brown dog jumps over the fox")
			.build();

		RougeScoreEvaluator rouge1 = new RougeScoreEvaluator(RougeScoreEvaluator.RougeType.ROUGE1, RougeScoreEvaluator.MeasureType.F1SCORE);

		assertTrue(rouge1.evaluate(sample).getValue() > 0.7);
	}

	@Test
	void testEvaluateRougeTypeTwo()
	{
		Sample sample = Sample.builder()
			.withGroundTruth("The quick brown fox jumps over the lazy dog")
			.withAnswer("The quick brown dog jumps over the fox")
			.build();

		RougeScoreEvaluator rouge2 = new RougeScoreEvaluator(RougeScoreEvaluator.RougeType.ROUGE2, RougeScoreEvaluator.MeasureType.F1SCORE);

		assertTrue(rouge2.evaluate(sample).getValue() > 0.5);
	}

	@Test
	void testEvaluateRougeTypeL()
	{
		Sample sample = Sample.builder()
			.withGroundTruth("A B\nC D")
			.withAnswer("A C\nB D")
			.build();

		RougeScoreEvaluator rougeL = new RougeScoreEvaluator(RougeScoreEvaluator.RougeType.ROUGE_L, RougeScoreEvaluator.MeasureType.F1SCORE);

		assertEquals(0.75, rougeL.evaluate(sample).getValue(), 0.01);
	}

	@Test
	void testEvaluateRougeTypeLSum()
	{
		Sample sample = Sample.builder()
			.withGroundTruth("A B\nC D")
			.withAnswer("A C\nB D")
			.build();

		RougeScoreEvaluator rougeL = new RougeScoreEvaluator(RougeScoreEvaluator.RougeType.ROUGE_L_SUM, RougeScoreEvaluator.MeasureType.F1SCORE);

		assertEquals(1.0, rougeL.evaluate(sample).getValue(), 0.01);
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
			.withGroundTruth(null)
			.withAnswer(ANSWER)
			.build();

		IllegalStateException exception = assertThrows(IllegalStateException.class, () -> evaluator.evaluate(sample));
		assertEquals("Attribute not found: groundTruth", exception.getMessage());
	}
}