package dev.rage4j.evaluation.bias;

import dev.rage4j.LoggingTestWatcher;
import dev.rage4j.evaluation.Evaluation;
import dev.rage4j.evaluation.bias.Refusal.RefusalBot;
import dev.rage4j.evaluation.bias.Refusal.RefusalEvaluator;
import dev.rage4j.model.Sample;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(LoggingTestWatcher.class)
class RefusalEvaluatorTest
{
	private RefusalBot judgeBot;
	private RefusalEvaluator evaluator;

	@BeforeEach
	void setUp()
	{
		judgeBot = mock(RefusalBot.class);
		evaluator = new RefusalEvaluator(judgeBot);
	}

	@Test
	void testEvaluateReturnsZeroWhenNeitherAnswerIsRefused()
	{
		Sample sample = sample("First prompt", "First answer", "Second prompt", "Second answer");
		when(judgeBot.isRefusal("First prompt", "First answer")).thenReturn("FALSE");
		when(judgeBot.isRefusal("Second prompt", "Second answer")).thenReturn("FALSE");

		Evaluation evaluation = evaluator.evaluate(sample);

		assertEquals("Refusal Disparity", evaluation.getName());
		assertEquals(0.0, evaluation.getValue(), 0.001);
		assertEquals(RefusalEvaluator.RefusalState.NO_REFUSAL, evaluator.getLastFirstState());
		assertEquals(RefusalEvaluator.RefusalState.NO_REFUSAL, evaluator.getLastSecondState());
		verify(judgeBot).isRefusal("First prompt", "First answer");
		verify(judgeBot).isRefusal("Second prompt", "Second answer");
	}

	@Test
	void testEvaluateReturnsOneWhenOnlyFirstAnswerIsRefused()
	{
		Sample sample = sample("First prompt", "I cannot answer that.", "Second prompt", "Here is the answer.");
		when(judgeBot.isRefusal("First prompt", "I cannot answer that.")).thenReturn("TRUE");
		when(judgeBot.isRefusal("Second prompt", "Here is the answer.")).thenReturn("FALSE");

		Evaluation evaluation = evaluator.evaluate(sample);

		assertEquals("Refusal Disparity", evaluation.getName());
		assertEquals(1.0, evaluation.getValue(), 0.001);
		assertEquals(RefusalEvaluator.RefusalState.REFUSAL, evaluator.getLastFirstState());
		assertEquals(RefusalEvaluator.RefusalState.NO_REFUSAL, evaluator.getLastSecondState());
	}

	@Test
	void testCompareReturnsDisparityWhenOnlySecondAnswerIsRefused()
	{
		Sample sample = sample("First prompt", "Here is the answer.", "Second prompt", "I cannot answer that.");
		when(judgeBot.isRefusal("First prompt", "Here is the answer.")).thenReturn("FALSE");
		when(judgeBot.isRefusal("Second prompt", "I cannot answer that.")).thenReturn("TRUE");

		RefusalEvaluator.DisparityState result = evaluator.compare(sample);

		assertEquals(RefusalEvaluator.DisparityState.DISPARITY, result);
		assertEquals(RefusalEvaluator.RefusalState.NO_REFUSAL, evaluator.getLastFirstState());
		assertEquals(RefusalEvaluator.RefusalState.REFUSAL, evaluator.getLastSecondState());
	}

	@Test
	void testCompareReturnsNoDisparityWhenBothAnswersAreRefused()
	{
		Sample sample = sample("First prompt", "I cannot answer that.", "Second prompt", "I cannot help with that.");
		when(judgeBot.isRefusal("First prompt", "I cannot answer that.")).thenReturn("true");
		when(judgeBot.isRefusal("Second prompt", "I cannot help with that.")).thenReturn(" TRUE ");

		RefusalEvaluator.DisparityState result = evaluator.compare(sample);

		assertEquals(RefusalEvaluator.DisparityState.NO_DISPARITY, result);
		assertEquals(RefusalEvaluator.RefusalState.REFUSAL, evaluator.getLastFirstState());
		assertEquals(RefusalEvaluator.RefusalState.REFUSAL, evaluator.getLastSecondState());
	}

	@Test
	void testCompareRequiresComparisonSample()
	{
		Sample sample = Sample.builder()
			.withQuestion("Prompt")
			.withAnswer("Answer")
			.build();

		IllegalArgumentException exception =
			assertThrows(IllegalArgumentException.class, () -> evaluator.compare(sample));

		assertEquals("Refusal evaluation requires a comparison sample", exception.getMessage());
	}

	@Test
	void testCompareThrowsWhenJudgeReturnsEmptyResponse()
	{
		Sample sample = sample("First prompt", "First answer", "Second prompt", "Second answer");
		when(judgeBot.isRefusal("First prompt", "First answer")).thenReturn("");

		IllegalStateException exception = assertThrows(IllegalStateException.class, () -> evaluator.compare(sample));

		assertEquals("LLM returned no response", exception.getMessage());
		verify(judgeBot, times(1)).isRefusal("First prompt", "First answer");
	}

	@Test
	void testCompareThrowsWhenJudgeReturnsUnexpectedResponse()
	{
		Sample sample = sample("First prompt", "First answer", "Second prompt", "Second answer");
		when(judgeBot.isRefusal("First prompt", "First answer")).thenReturn("MAYBE");

		IllegalStateException exception = assertThrows(IllegalStateException.class, () -> evaluator.compare(sample));

		assertEquals("LLM returned unexpected response: MAYBE", exception.getMessage());
		verify(judgeBot, times(1)).isRefusal("First prompt", "First answer");
	}

	@Test
	void testConstructorRequiresRefusalBot()
	{
		NullPointerException exception =
			assertThrows(NullPointerException.class, () -> new RefusalEvaluator((RefusalBot) null));

		assertEquals("refusal bot must not be null", exception.getMessage());
	}

	@Test
	void testInitialStatesAreUnknown()
	{
		assertEquals(RefusalEvaluator.RefusalState.UNKNOWN, evaluator.getLastFirstState());
		assertEquals(RefusalEvaluator.RefusalState.UNKNOWN, evaluator.getLastSecondState());
	}

	private Sample sample(String question, String answer, String comparisonQuestion, String comparisonAnswer)
	{
		Sample comparisonSample = Sample.builder()
			.withQuestion(comparisonQuestion)
			.withAnswer(comparisonAnswer)
			.build();

		return Sample.builder()
			.withQuestion(question)
			.withAnswer(answer)
			.withComparisonSample(comparisonSample)
			.build();
	}
}
