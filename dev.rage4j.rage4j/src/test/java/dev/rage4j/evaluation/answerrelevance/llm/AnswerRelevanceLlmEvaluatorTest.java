package dev.rage4j.evaluation.answerrelevance.llm;

import dev.rage4j.LoggingTestWatcher;
import dev.rage4j.evaluation.Evaluation;
import dev.rage4j.evaluation.model.ScoreWithReasonResponse;
import dev.rage4j.model.Sample;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(LoggingTestWatcher.class)
class AnswerRelevanceLlmEvaluatorTest
{
	private static final String QUESTION = "What is the capital of France?";
	private static final String ANSWER = "Paris is the capital of France.";
	private static final String IRRELEVANT_ANSWER = "The sun is a star.";

	private AnswerRelevanceLlmEvaluator evaluator;
	private AnswerRelevanceLlmBot mockBot;
	private Sample sample;

	@BeforeEach
	void setUp()
	{
		mockBot = mock(AnswerRelevanceLlmBot.class);
		evaluator = new AnswerRelevanceLlmEvaluator(mockBot);
		sample = Sample.builder()
			.withQuestion(QUESTION)
			.withAnswer(ANSWER)
			.build();
	}

	@Test
	void testEvaluateRelevancePerfectScore()
	{
		when(mockBot.generateScoreWithReason(QUESTION, ANSWER)).thenReturn(new ScoreWithReasonResponse(3, "Fully answers the question"));

		Evaluation result = evaluator.evaluate(sample);

		assertEquals(3, result.getValue());
		assertEquals("Answer relevance llm", result.getName());
	}

	@Test
	void testEvaluateRelevanceMostlyRelevant()
	{
		when(mockBot.generateScoreWithReason(QUESTION, ANSWER)).thenReturn(new ScoreWithReasonResponse(2, "Mostly relevant with minor omission"));

		Evaluation result = evaluator.evaluate(sample);

		assertEquals(2, result.getValue());
		assertEquals("Answer relevance llm", result.getName());
	}

	@Test
	void testEvaluateRelevancePartiallyRelevant()
	{
		when(mockBot.generateScoreWithReason(QUESTION, IRRELEVANT_ANSWER)).thenReturn(new ScoreWithReasonResponse(1, "Touches only a small relevant aspect"));

		Sample irrelevantSample = Sample.builder()
			.withQuestion(QUESTION)
			.withAnswer(IRRELEVANT_ANSWER)
			.build();

		Evaluation result = evaluator.evaluate(irrelevantSample);

		assertEquals(1, result.getValue());
		assertEquals("Answer relevance llm", result.getName());
	}

	@Test
	void testEvaluateRelevanceNoRelevance()
	{
		when(mockBot.generateScoreWithReason(QUESTION, IRRELEVANT_ANSWER)).thenReturn(new ScoreWithReasonResponse(0, "Does not answer the question"));

		Sample irrelevantSample = Sample.builder()
			.withQuestion(QUESTION)
			.withAnswer(IRRELEVANT_ANSWER)
			.build();

		Evaluation result = evaluator.evaluate(irrelevantSample);

		assertEquals(0, result.getValue());
		assertEquals("Answer relevance llm", result.getName());
	}

	@Test
	void testEvaluateRelevanceLlmNullAnswer()
	{
		Sample nullAnswerSample = Sample.builder()
			.withQuestion(QUESTION)
			.withAnswer(null)
			.build();

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> evaluator.evaluate(nullAnswerSample));
		assertEquals("Sample must have an answer for Answer Relevance LLM evaluation", exception.getMessage());
	}

	@Test
	void testEvaluateRelevanceLlmNullQuestion()
	{
		Sample nullQuestionSample = Sample.builder()
			.withAnswer(ANSWER)
			.withQuestion(null)
			.build();

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> evaluator.evaluate(nullQuestionSample));
		assertEquals("Sample must have a question for Answer Relevance LLM evaluation", exception.getMessage());
	}

	@Test
	void testEvaluateRelevanceFallsBackToNumericScore()
	{
		when(mockBot.generateScoreWithReason(QUESTION, ANSWER)).thenThrow(new IllegalStateException("Structured parse failed"));
		when(mockBot.generateScore(QUESTION, ANSWER)).thenReturn("2");

		Evaluation result = evaluator.evaluate(sample);

		assertEquals(2, result.getValue());
		assertEquals("Answer relevance llm", result.getName());
	}
}
