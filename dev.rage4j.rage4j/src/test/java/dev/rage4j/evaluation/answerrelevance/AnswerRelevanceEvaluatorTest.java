package dev.rage4j.evaluation.answerrelevance;

import dev.rage4j.LoggingTestWatcher;
import dev.rage4j.evaluation.Evaluation;
import dev.rage4j.evaluation.model.ArrayResponse;
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
class AnswerRelevanceEvaluatorTest
{
	private static final String QUESTION = "What is the capital of France?";
	private static final String ANSWER = "Paris is the capital of France.";
	private static final double EXPECTED_SCORE = 0.95;
	private static final String[] GENERATED_QUESTIONS = {
		"What is the capital of France?",
		"Which city is the capital of France?"
	};

	private AnswerRelevanceEvaluator evaluator;
	private AnswerRelevanceBot mockBot;
	private BiFunction<String, String, Double> mockSimilarityComputer;
	private Sample sample;

	@BeforeEach
	void setUp()
	{
		mockBot = mock(AnswerRelevanceBot.class);
		mockSimilarityComputer = mock(BiFunction.class);

		evaluator = new AnswerRelevanceEvaluator(mockBot, mockSimilarityComputer);

		sample = Sample.builder()
			.withQuestion(QUESTION)
			.withAnswer(ANSWER)
			.build();
	}

	@Test
	void testEvaluateRelevanceFullSimilarity()
	{
		when(mockBot.getGeneratedQuestions(ANSWER)).thenReturn(new ArrayResponse(GENERATED_QUESTIONS));
		when(mockSimilarityComputer.apply(QUESTION, GENERATED_QUESTIONS[0])).thenReturn(1.0);
		when(mockSimilarityComputer.apply(QUESTION, GENERATED_QUESTIONS[1])).thenReturn(0.9);

		Evaluation result = evaluator.evaluate(sample);

		assertEquals(EXPECTED_SCORE, result.getValue(), 0.01);
		assertEquals("Answer relevance", result.getName());
	}

	@Test
	void testEvaluateRelevanceNoSimilarity()
	{
		when(mockBot.getGeneratedQuestions(ANSWER)).thenReturn(new ArrayResponse(GENERATED_QUESTIONS));
		when(mockSimilarityComputer.apply(QUESTION, GENERATED_QUESTIONS[0])).thenReturn(0.0);
		when(mockSimilarityComputer.apply(QUESTION, GENERATED_QUESTIONS[1])).thenReturn(0.0);

		Evaluation result = evaluator.evaluate(sample);

		assertEquals(0.0, result.getValue(), 0.01);
		assertEquals("Answer relevance", result.getName());
	}

	@Test
	void testEvaluateRelevanceEmptyGeneratedQuestions()
	{
		when(mockBot.getGeneratedQuestions(ANSWER)).thenReturn(new ArrayResponse(new String[] {}));

		Evaluation result = evaluator.evaluate(sample);

		assertEquals(0.0, result.getValue(), 0.01);
		assertEquals("Answer relevance", result.getName());
	}

	@Test
	void testEvaluateRelevanceNullAnswer()
	{
		Sample nullAnswerSample = Sample.builder()
			.withQuestion(QUESTION)
			.withAnswer(null)
			.build();

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> evaluator.evaluate(nullAnswerSample));
		assertEquals("Sample must have an answer for Answer Relevance evaluation", exception.getMessage());
	}

	@Test
	void testEvaluateRelevanceNullQuestion()
	{
		Sample nullQuestionSample = Sample.builder()
			.withAnswer(ANSWER)
			.withQuestion(null)
			.build();

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> evaluator.evaluate(nullQuestionSample));
		assertEquals("Sample must have a question for Answer Relevance evaluation", exception.getMessage());
	}
}
