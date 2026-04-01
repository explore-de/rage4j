package dev.rage4j.evaluation.bias;

import dev.rage4j.LoggingTestWatcher;
import dev.langchain4j.model.chat.ChatModel;
import dev.rage4j.evaluation.Evaluation;
import dev.rage4j.evaluation.bias.Refusal.RefusalBot;
import dev.rage4j.evaluation.bias.Refusal.RefusalEvaluator.DisparityState;
import dev.rage4j.evaluation.bias.Refusal.RefusalEvaluator;
import dev.rage4j.model.Sample;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
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
	private ChatModel mockEvaluatedModel;

	@BeforeEach
	void setUp()
	{
		mockBot = mock(RefusalBot.class);
		mockEvaluatedModel = mock(ChatModel.class);
		regexEvaluator = new RefusalEvaluator();
		llmEvaluator = new RefusalEvaluator(mockBot);
	}

	@Test
	void testEvaluateRequiresComparisonSample()
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

		assertEquals(RefusalEvaluator.RefusalState.REFUSAL, regexEvaluator.checkRefusal(sample.getAnswerOrFail()));
	}

	@Test
	void testIsRefusalFalseWithRegex()
	{
		Sample sample = Sample.builder()
			.withQuestion(FALSE_REFUSAL_QUESTION)
			.withAnswer(ANSWER_OK)
			.build();

		assertEquals(RefusalEvaluator.RefusalState.NO_REFUSAL, regexEvaluator.checkRefusal(sample.getAnswerOrFail()));
	}

	@Test
	void testIsRefusalTrueWithLlm()
	{
		Sample sample = Sample.builder()
			.withQuestion(TRUE_REFUSAL_QUESTION)
			.withAnswer("Ich kann dabei nicht helfen.")
			.build();

		when(mockBot.isRefusal("Ich kann dabei nicht helfen.")).thenReturn("TRUE");

		assertEquals(RefusalEvaluator.RefusalState.REFUSAL, llmEvaluator.checkRefusal(sample.getAnswerOrFail()));
		verify(mockBot).isRefusal("Ich kann dabei nicht helfen.");
	}

	@Test
	void testIsRefusalFalseWithLlm()
	{
		Sample sample = Sample.builder()
			.withQuestion(FALSE_REFUSAL_QUESTION)
			.withAnswer("Hier ist eine harmlose Geschichte.")
			.build();

		when(mockBot.isRefusal("Hier ist eine harmlose Geschichte.")).thenReturn("FALSE");

		assertEquals(RefusalEvaluator.RefusalState.NO_REFUSAL, llmEvaluator.checkRefusal(sample.getAnswerOrFail()));
		verify(mockBot).isRefusal("Hier ist eine harmlose Geschichte.");
	}

	@Test
	void testIsRefusalFalseWithLlmIgnoresRegexMatch()
	{
		Sample sample = Sample.builder()
			.withQuestion(TRUE_REFUSAL_QUESTION)
			.withAnswer(ANSWER_REFUSAL)
			.build();

		when(mockBot.isRefusal(ANSWER_REFUSAL)).thenReturn("FALSE");

		assertEquals(RefusalEvaluator.RefusalState.NO_REFUSAL, llmEvaluator.checkRefusal(sample.getAnswerOrFail()));
		verify(mockBot).isRefusal(ANSWER_REFUSAL);
	}

	@Test
	void testDetectRefusalUnknownWithLlmWhenBotReturnsNull()
	{
		Sample sample = Sample.builder()
			.withQuestion(TRUE_REFUSAL_QUESTION)
			.withAnswer(ANSWER_REFUSAL)
			.build();

		when(mockBot.isRefusal(ANSWER_REFUSAL)).thenReturn(null);

		assertEquals(RefusalEvaluator.RefusalState.UNKNOWN, llmEvaluator.checkRefusal(sample.getAnswerOrFail()));
		verify(mockBot).isRefusal(ANSWER_REFUSAL);
	}

	@Test
	void testDetectRefusalUnknownWithLlmWhenBotReturnsUnexpectedValue()
	{
		Sample sample = Sample.builder()
			.withQuestion(TRUE_REFUSAL_QUESTION)
			.withAnswer(ANSWER_REFUSAL)
			.build();

		when(mockBot.isRefusal(ANSWER_REFUSAL)).thenReturn("MAYBE");

		assertEquals(RefusalEvaluator.RefusalState.UNKNOWN, llmEvaluator.checkRefusal(sample.getAnswerOrFail()));
		verify(mockBot).isRefusal(ANSWER_REFUSAL);
	}

	@Test
	void testDetectRefusalUsesEvaluatedModelWhenAnswerIsMissing()
	{
		RefusalEvaluator evaluator = new RefusalEvaluator(mockEvaluatedModel);
		Sample sample = Sample.builder()
			.withQuestion(TRUE_REFUSAL_QUESTION)
			.withComparisonSample(Sample.builder()
				.withQuestion(FALSE_REFUSAL_QUESTION)
				.withAnswer(ANSWER_OK)
				.build())
			.build();

		when(mockEvaluatedModel.chat(TRUE_REFUSAL_QUESTION)).thenReturn(ANSWER_REFUSAL);

		assertEquals(DisparityState.DISPARITY, evaluator.compare(sample));
		verify(mockEvaluatedModel).chat(TRUE_REFUSAL_QUESTION);
	}

	@Test
	void testEvaluateUsesEvaluatedModelWhenAnswersAreMissing()
	{
		RefusalEvaluator evaluator = new RefusalEvaluator(mockEvaluatedModel);
		Sample comparisonSample = Sample.builder()
			.withQuestion(FALSE_REFUSAL_QUESTION)
			.build();

		Sample testSample = Sample.builder()
			.withQuestion(TRUE_REFUSAL_QUESTION)
			.withComparisonSample(comparisonSample)
			.build();

		when(mockEvaluatedModel.chat(TRUE_REFUSAL_QUESTION)).thenReturn(ANSWER_REFUSAL);
		when(mockEvaluatedModel.chat(FALSE_REFUSAL_QUESTION)).thenReturn(ANSWER_OK);

		Evaluation result = evaluator.evaluate(testSample);

		assertEquals(1.0, result.getValue());
		verify(mockEvaluatedModel).chat(TRUE_REFUSAL_QUESTION);
		verify(mockEvaluatedModel).chat(FALSE_REFUSAL_QUESTION);
	}

	@Test
	void testEvaluateRefusalDisparityTrue()
	{
		Sample comparisonSample = Sample.builder()
			.withQuestion(FALSE_REFUSAL_QUESTION)
			.withAnswer(ANSWER_OK)
			.build();

		Sample testSample = Sample.builder()
			.withQuestion(TRUE_REFUSAL_QUESTION)
			.withAnswer(ANSWER_REFUSAL)
			.withComparisonSample(comparisonSample)
			.build();

		Evaluation result = regexEvaluator.evaluate(testSample);

		assertEquals(1.0, result.getValue());
	}

	@Test
	void testEvaluateRefusalDisparityFalse()
	{
		Sample comparisonSample = Sample.builder()
			.withQuestion(FALSE_REFUSAL_QUESTION)
			.withAnswer(ANSWER_OK)
			.build();

		Sample testSample = Sample.builder()
			.withQuestion(FALSE_REFUSAL_QUESTION)
			.withAnswer(ANSWER_OK)
			.withComparisonSample(comparisonSample)
			.build();

		Evaluation result = regexEvaluator.evaluate(testSample);

		assertEquals(0.0, result.getValue());
	}

	@Test
	void testEvaluateThrowsWhenDisparityStateIsUnknown()
	{
		Sample comparisonSample = Sample.builder()
			.withQuestion(FALSE_REFUSAL_QUESTION)
			.withAnswer(ANSWER_OK)
			.build();

		Sample testSample = Sample.builder()
			.withQuestion(TRUE_REFUSAL_QUESTION)
			.withAnswer(ANSWER_REFUSAL)
			.withComparisonSample(comparisonSample)
			.build();

		when(mockBot.isRefusal(ANSWER_REFUSAL)).thenReturn(null);
		when(mockBot.isRefusal(ANSWER_OK)).thenReturn("FALSE");

		assertThrows(IllegalStateException.class, () -> llmEvaluator.evaluate(testSample));
	}
}
