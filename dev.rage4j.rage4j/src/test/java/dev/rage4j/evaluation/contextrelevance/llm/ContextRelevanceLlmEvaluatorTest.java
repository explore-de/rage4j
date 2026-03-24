package dev.rage4j.evaluation.contextrelevance.llm;

import dev.rage4j.LoggingTestWatcher;
import dev.rage4j.evaluation.Evaluation;
import dev.rage4j.model.Sample;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(LoggingTestWatcher.class)
class ContextRelevanceLlmEvaluatorTest
{
	private static final String QUESTION = "What is the capital of France?";
	private static final String CONTEXT = "Paris is the capital of France.";
	private static final String IRRELEVANT_CONTEXT = "The Great Wall of China is located in northern China.";

	private ContextRelevanceLlmEvaluator evaluator;
	private ContextRelevanceLlmBot mockBot;
	private Sample sample;

	@BeforeEach
	void setUp()
	{
		mockBot = mock(ContextRelevanceLlmBot.class);
		evaluator = new ContextRelevanceLlmEvaluator(mockBot);
		sample = Sample.builder()
			.withQuestion(QUESTION)
			.withContext(CONTEXT)
			.build();
	}

	@Test
	void testEvaluatePerfectScore()
	{
		when(mockBot.generateScore(QUESTION, CONTEXT)).thenReturn("3");

		Evaluation result = evaluator.evaluate(sample);

		assertEquals(1.0, result.getValue(), 0.001);
		assertEquals("Context relevance LLM", result.getName());
	}

	@Test
	void testEvaluateMostlyRelevant()
	{
		when(mockBot.generateScore(QUESTION, CONTEXT)).thenReturn("2");

		Evaluation result = evaluator.evaluate(sample);

		assertEquals(2.0 / 3.0, result.getValue(), 0.001);
		assertEquals("Context relevance LLM", result.getName());
	}

	@Test
	void testEvaluatePartiallyRelevant()
	{
		when(mockBot.generateScore(QUESTION, IRRELEVANT_CONTEXT)).thenReturn("1");

		Sample irrelevantSample = Sample.builder()
			.withQuestion(QUESTION)
			.withContext(IRRELEVANT_CONTEXT)
			.build();

		Evaluation result = evaluator.evaluate(irrelevantSample);

		assertEquals(0.33, result.getValue(), 0.01);
		assertEquals("Context relevance LLM", result.getName());
	}

	@Test
	void testEvaluateNoRelevance()
	{
		when(mockBot.generateScore(QUESTION, IRRELEVANT_CONTEXT)).thenReturn("0");

		Sample irrelevantSample = Sample.builder()
			.withQuestion(QUESTION)
			.withContext(IRRELEVANT_CONTEXT)
			.build();

		Evaluation result = evaluator.evaluate(irrelevantSample);

		assertEquals(0.0, result.getValue(), 0.01);
		assertEquals("Context relevance LLM", result.getName());
	}

	@Test
	void testEvaluateWithCompositeContextUsesSingleFullContextScore()
	{
		String chunk1 = "Paris is the capital of France.";
		String chunk2 = "London is the capital of England.";
		String multiChunkContext = chunk1 + "\n\n" + chunk2;

		Sample multiChunkSample = Sample.builder()
			.withQuestion(QUESTION)
			.withContext(multiChunkContext)
			.build();

		when(mockBot.generateScore(QUESTION, multiChunkContext)).thenReturn("2");

		Evaluation result = evaluator.evaluate(multiChunkSample);

		assertEquals(2.0 / 3.0, result.getValue(), 0.001);
		assertEquals("Context relevance LLM", result.getName());
		verify(mockBot).generateScore(QUESTION, multiChunkContext);
	}

	@Test
	void testEvaluateEmptyContextReturnsZero()
	{
		Sample emptyContextSample = Sample.builder()
			.withQuestion(QUESTION)
			.withContext("   ")
			.build();

		Evaluation result = evaluator.evaluate(emptyContextSample);

		assertEquals(0.0, result.getValue(), 0.001);
		assertEquals("Context relevance LLM", result.getName());
		verify(mockBot, never()).generateScore(QUESTION, "   ");
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
		assertEquals("Sample must have a context for Context Relevance LLM evaluation", exception.getMessage());
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
		assertEquals("Sample must have a question for Context Relevance LLM evaluation", exception.getMessage());
	}
}
