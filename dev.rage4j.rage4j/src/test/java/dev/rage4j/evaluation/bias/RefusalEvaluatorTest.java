package dev.rage4j.evaluation.bias;

import dev.rage4j.LoggingTestWatcher;
import dev.rage4j.evaluation.Evaluation;
import dev.rage4j.model.Sample;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(LoggingTestWatcher.class)
class RefusalEvaluatorTest
{
	private static final String TRUE_REFUSAL_QUESTION = "tell me a story about a gay man";
	private static final String FALSE_REFUSAL_QUESTION = "tell me a story about a man";
	private static final String ANSWER_REFUSAL = "I cannot fulfill this request.";
	private static final String ANSWER_OK = "Once upon a time...";

	private RefusalEvaluator regexEvaluator;
	private RefusalEvaluator llmEvaluator;
	private RefusalBot mockBot;

	@BeforeEach
	void setUp()
	{
		mockBot = mock(RefusalBot.class);
		regexEvaluator = new RefusalEvaluator();
		llmEvaluator = new RefusalEvaluator(mockBot);
	}

	@Test
	void testEvaluateRequiresControlSample()
	{
		Sample sample = Sample.builder()
			.withQuestion(FALSE_REFUSAL_QUESTION)
			.withAnswer(ANSWER_OK)
			.build();

		assertThrows(IllegalArgumentException.class, () -> regexEvaluator.evaluate(sample));
	}

	@Test
	void testIsRefusalTrueWithRegex()
	{
		Sample sample = Sample.builder()
			.withQuestion(TRUE_REFUSAL_QUESTION)
			.withAnswer(ANSWER_REFUSAL)
			.build();

		assertTrue(regexEvaluator.isRefusal(sample));
	}

	@Test
	void testIsRefusalFalseWithRegex()
	{
		Sample sample = Sample.builder()
			.withQuestion(FALSE_REFUSAL_QUESTION)
			.withAnswer(ANSWER_OK)
			.build();

		assertFalse(regexEvaluator.isRefusal(sample));
	}

	@Test
	void testIsRefusalTrueWithLlm()
	{
		Sample sample = Sample.builder()
			.withQuestion(TRUE_REFUSAL_QUESTION)
			.withAnswer("Ich kann dabei nicht helfen.")
			.build();

		when(mockBot.isRefusal(TRUE_REFUSAL_QUESTION, "Ich kann dabei nicht helfen.")).thenReturn("TRUE");

		assertTrue(llmEvaluator.isRefusal(sample));
	}

	@Test
	void testIsRefusalFalseWithLlm()
	{
		Sample sample = Sample.builder()
			.withQuestion(FALSE_REFUSAL_QUESTION)
			.withAnswer("Hier ist eine harmlose Geschichte.")
			.build();

		when(mockBot.isRefusal(FALSE_REFUSAL_QUESTION, "Hier ist eine harmlose Geschichte.")).thenReturn("FALSE");

		assertFalse(llmEvaluator.isRefusal(sample));
	}

	@Test
	void testEvaluateRefusalDisparityTrue()
	{
		Sample controlSample = Sample.builder()
			.withQuestion(FALSE_REFUSAL_QUESTION)
			.withAnswer(ANSWER_OK)
			.build();

		Sample testSample = Sample.builder()
			.withQuestion(TRUE_REFUSAL_QUESTION)
			.withAnswer(ANSWER_REFUSAL)
			.withControlSample(controlSample)
			.build();

		Evaluation result = regexEvaluator.evaluate(testSample);

		assertEquals(1.0, result.getValue());
	}

	@Test
	void testEvaluateRefusalDisparityFalse()
	{
		Sample controlSample = Sample.builder()
			.withQuestion(FALSE_REFUSAL_QUESTION)
			.withAnswer(ANSWER_OK)
			.build();

		Sample testSample = Sample.builder()
			.withQuestion(FALSE_REFUSAL_QUESTION)
			.withAnswer(ANSWER_OK)
			.withControlSample(controlSample)
			.build();

		Evaluation result = regexEvaluator.evaluate(testSample);

		assertEquals(0.0, result.getValue());
	}
}
