package dev.rage4j.evaluation.answersemanicsimilarity;

import dev.rage4j.LoggingTestWatcher;
import dev.rage4j.evaluation.Evaluation;
import dev.rage4j.evaluation.answersemanticsimilarity.AnswerSemanticSimilarityEvaluator;
import dev.rage4j.model.Sample;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(LoggingTestWatcher.class)
class AnswerSemanticSimilarityEvaluatorTest
{
	private static final String ANSWER = "Paris is the capital of France.";
	private static final String GROUND_TRUTH = "The capital of France is Paris.";

	private AnswerSemanticSimilarityEvaluator evaluator;
	private BiFunction<String, String, Double> mockStringSimilarityComputer;
	private Sample sample;

	@BeforeEach
	void setUp()
	{
		mockStringSimilarityComputer = mock(BiFunction.class);
		evaluator = new AnswerSemanticSimilarityEvaluator(mockStringSimilarityComputer);

		sample = Sample.builder()
			.withAnswer(ANSWER)
			.withGroundTruth(GROUND_TRUTH)
			.build();
	}

	@Test
	void testEvaluateWithHighSimilarity()
	{
		when(mockStringSimilarityComputer.apply(ANSWER, GROUND_TRUTH)).thenReturn(1.0);

		Evaluation result = evaluator.evaluate(sample);

		assertEquals(1.0, result.getValue(), 0.01);
		assertEquals("Answer semantic similarity", result.getName());
	}

	@Test
	void testEvaluateWithLowSimilarity()
	{
		double lowSimilarity = 0.2;
		when(mockStringSimilarityComputer.apply(ANSWER, GROUND_TRUTH)).thenReturn(lowSimilarity);

		Evaluation result = evaluator.evaluate(sample);

		assertEquals(lowSimilarity, result.getValue(), 0.01);
		assertEquals("Answer semantic similarity", result.getName());
	}

	@Test
	void testEvaluateWithNoSimilarity()
	{
		double noSimilarity = 0.0;
		when(mockStringSimilarityComputer.apply(ANSWER, GROUND_TRUTH)).thenReturn(noSimilarity);

		Evaluation result = evaluator.evaluate(sample);

		assertEquals(noSimilarity, result.getValue(), 0.01);
		assertEquals("Answer semantic similarity", result.getName());
	}

	@Test
	void testEvaluateNullAnswerThrowsException()
	{
		Sample nullAnswerSample = Sample.builder()
			.withGroundTruth(GROUND_TRUTH)
			.withAnswer(null)
			.build();

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> evaluator.evaluate(nullAnswerSample));
		assertEquals("Sample must have an answer for Answer Semantic Similarity evaluation", exception.getMessage());
	}

	@Test
	void testEvaluateNullGroundTruthThrowsException()
	{
		Sample nullGroundTruthSample = Sample.builder()
			.withAnswer(ANSWER)
			.withGroundTruth(null)
			.build();

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> evaluator.evaluate(nullGroundTruthSample));
		assertEquals("Sample must have a ground truth for Answer Semantic Similarity evaluation", exception.getMessage());
	}
}
