package dev.rage4j.evaluation.contextrelevance.embedding;

import dev.rage4j.LoggingTestWatcher;
import dev.rage4j.evaluation.Evaluation;
import dev.rage4j.model.Sample;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(LoggingTestWatcher.class)
class ContextRelevanceEmbeddingEvaluatorTest
{
	private static final String QUESTION = "What is the capital of France?";
	private static final String CONTEXT = "Paris is the capital of France.";

	private ContextRelevanceEmbeddingEvaluator evaluator;
	private BiFunction<String, List<String>, List<Double>>  mockSimilarityBatchComputer;
	private Sample sample;

	@BeforeEach
	void setUp()
	{
		mockSimilarityBatchComputer = mock(BiFunction.class);
		evaluator = new ContextRelevanceEmbeddingEvaluator(mockSimilarityBatchComputer);

		sample = Sample.builder()
			.withQuestion(QUESTION)
			.withContext(CONTEXT)
			.build();
	}

//	@Test
//	void testEvaluateWithHighSimilarity()
//	{
//		when(mockSimilarityBatchComputer.apply(QUESTION, CONTEXT)).thenReturn(0.95);
//
//		Evaluation result = evaluator.evaluate(sample);
//
//		assertEquals(0.95, result.getValue(), 0.01);
//		assertEquals("context relevance embedding", result.getName());
//	}
//
//	@Test
//	void testEvaluateWithLowSimilarity()
//	{
//		when(mockSimilarityBatchComputer.apply(QUESTION, CONTEXT)).thenReturn(0.2);
//
//		Evaluation result = evaluator.evaluate(sample);
//
//		assertEquals(0.2, result.getValue(), 0.01);
//		assertEquals("context relevance embedding", result.getName());
//	}
//
//	@Test
//	void testEvaluateWithNoSimilarity()
//	{
//		when(mockSimilarityBatchComputer.apply(QUESTION, CONTEXT)).thenReturn(0.0);
//
//		Evaluation result = evaluator.evaluate(sample);
//
//		assertEquals(0.0, result.getValue(), 0.01);
//		assertEquals("context relevance embedding", result.getName());
//	}
//
//	@Test
//	void testEvaluateWithMultipleChunksReturnsBest()
//	{
//		String chunk1 = "Paris is the capital of France.";
//		String chunk2 = "London is the capital of England.";
//		String multiChunkContext = chunk1 + "\n\n" + chunk2;
//
//		Sample multiChunkSample = Sample.builder()
//			.withQuestion(QUESTION)
//			.withContext(multiChunkContext)
//			.build();
//
//		when(mockSimilarityBatchComputer.apply(QUESTION, chunk1)).thenReturn(0.9);
//		when(mockSimilarityBatchComputer.apply(QUESTION, chunk2)).thenReturn(0.3);
//
//		Evaluation result = evaluator.evaluate(multiChunkSample);
//
//		assertEquals(0.9, result.getValue(), 0.01);
//		assertEquals("context relevance embedding", result.getName());
//	}

	@Test
	void testEvaluateWithEmptyContextReturnsZero()
	{
		Sample emptyContextSample = Sample.builder()
			.withQuestion(QUESTION)
			.withContext("   ")
			.build();

		Evaluation result = evaluator.evaluate(emptyContextSample);

		assertEquals(0.0, result.getValue(), 0.01);
		assertEquals("context relevance embedding", result.getName());
	}

	@Test
	void testEvaluateNullQuestionThrowsException()
	{
		Sample nullQuestionSample = Sample.builder()
			.withQuestion(null)
			.withContext(CONTEXT)
			.build();

		IllegalArgumentException exception = assertThrows(
			IllegalArgumentException.class,
			() -> evaluator.evaluate(nullQuestionSample));
		assertEquals("Sample must have a question for Answer Relevance embedding evaluation", exception.getMessage());
	}

	@Test
	void testEvaluateNullContextThrowsException()
	{
		Sample nullContextSample = Sample.builder()
			.withQuestion(QUESTION)
			.withContext(null)
			.build();

		IllegalArgumentException exception = assertThrows(
			IllegalArgumentException.class,
			() -> evaluator.evaluate(nullContextSample));
		assertEquals("Sample must have context for Context Relevance embedding evaluation", exception.getMessage());
	}
}
