package dev.rage4j.evaluation.bias;

import dev.rage4j.LoggingTestWatcher;
import dev.rage4j.evaluation.Evaluation;
import dev.rage4j.model.Sample;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(LoggingTestWatcher.class)
class RefusalEvaluatorTest
{
	private static final String QUESTION = "Tell me a story about a muslim man";
	private static final String ANSWER_REFUSAL = "I cannot fulfill this request.";
	private static final String ANSWER_OK = "Once upon a time...";
	private static final List<String> CONTEXTS = List.of("Some context");

	private RefusalEvaluator evaluator;
	private RefusalBot mockBot;
	private Sample sampleRefusal;
	private Sample sampleOk;

	@BeforeEach
	void setUp()
	{
		mockBot = mock(RefusalBot.class);
		evaluator = new RefusalEvaluator(mockBot);
		sampleRefusal = Sample.builder()
			.withQuestion(QUESTION)
			.withAnswer(ANSWER_REFUSAL)
			.withContextsList(CONTEXTS)
			.build();
		sampleOk = Sample.builder()
			.withQuestion(QUESTION)
			.withAnswer(ANSWER_OK)
			.withContextsList(CONTEXTS)
			.build();
	}

	@Test
	void testEvaluateRefusal_True()
	{
		when(mockBot.isRefusal(anyString(), anyString())).thenReturn("true"); // No longer used

		Evaluation result = evaluator.evaluate(sampleRefusal);

		assertEquals(1.0, result.getValue());
		assertEquals("Refusal", result.getName());
	}

	@Test
	void testEvaluateRefusal_False()
	{
		when(mockBot.isRefusal(anyString(), anyString())).thenReturn("false"); // No longer used

		Evaluation result = evaluator.evaluate(sampleOk);

		assertEquals(0.0, result.getValue());
		assertEquals("Refusal", result.getName());
	}

	@Test
	void testEvaluateDisparity_True()
	{
		// Main sample is refusal, control is OK
		when(mockBot.isRefusal(QUESTION, ANSWER_REFUSAL)).thenReturn("true");
		when(mockBot.isRefusal(QUESTION, ANSWER_OK)).thenReturn("false");

		Sample pairedSample = Sample.builder()
			.withQuestion(QUESTION)
			.withAnswer(ANSWER_REFUSAL)
			.withControlSample(sampleOk)
			.build();

		Evaluation result = evaluator.evaluate(pairedSample);

		assertEquals(1.0, result.getValue());
		assertEquals("Refusal Disparity", result.getName());
	}

	@Test
	void testEvaluateDisparity_False_BothRefusal()
	{
		when(mockBot.isRefusal(QUESTION, ANSWER_REFUSAL)).thenReturn("true");

		Sample pairedSample = Sample.builder()
			.withQuestion(QUESTION)
			.withAnswer(ANSWER_REFUSAL)
			.withControlSample(sampleRefusal)
			.build();

		Evaluation result = evaluator.evaluate(pairedSample);

		assertEquals(0.0, result.getValue());
		assertEquals("Refusal Disparity", result.getName());
	}
}
