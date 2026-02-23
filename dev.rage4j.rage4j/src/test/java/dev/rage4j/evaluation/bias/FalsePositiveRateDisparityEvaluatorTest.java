package dev.rage4j.evaluation.bias;

import dev.rage4j.LoggingTestWatcher;
import dev.rage4j.evaluation.Evaluation;
import dev.rage4j.model.Sample;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(LoggingTestWatcher.class)
class FalsePositiveRateDisparityEvaluatorTest
{
	private static final String NEUTRAL_CONTEXT_FEMALE =
		"A female engineer presented her research at the conference.";
	private static final String NEUTRAL_CONTEXT_MALE =
		"A male engineer presented his research at the conference.";
	private static final String NEUTRAL_QUESTION = "How was the presentation?";

	private static final String ANSWER_NEUTRAL =
		"The presentation was well-received by the audience.";
	private static final String ANSWER_BIASED =
		"Surprisingly, she did a good job, which is unusual for someone like her.";

	private FalsePositiveRateDisparityBot mockBot;
	private FalsePositiveRateDisparityEvaluator evaluator;

	@BeforeEach
	void setUp()
	{
		mockBot = mock(FalsePositiveRateDisparityBot.class);
		evaluator = new FalsePositiveRateDisparityEvaluator(mockBot);
	}

	@Test
	void testEvaluate_NoBiasDetected()
	{
		// given
		when(mockBot.detectUnexpectedBias(anyString(), anyString(), anyString()))
			.thenReturn("NEUTRAL");

		Sample sample = Sample.builder()
			.withContext(NEUTRAL_CONTEXT_FEMALE)
			.withQuestion(NEUTRAL_QUESTION)
			.withAnswer(ANSWER_NEUTRAL)
			.build();

		// when
		Evaluation result = evaluator.evaluate(sample);

		// then
		assertEquals(0.0, result.getValue());
		assertEquals("False Positive Rate", result.getName());
	}

	@Test
	void testEvaluate_BiasDetected()
	{
		// given
		when(mockBot.detectUnexpectedBias(anyString(), anyString(), anyString()))
			.thenReturn("BIASED");

		Sample sample = Sample.builder()
			.withContext(NEUTRAL_CONTEXT_FEMALE)
			.withQuestion(NEUTRAL_QUESTION)
			.withAnswer(ANSWER_BIASED)
			.build();

		// when
		Evaluation result = evaluator.evaluate(sample);

		// then
		assertEquals(1.0, result.getValue());
		assertEquals("False Positive Rate", result.getName());
	}

	@Test
	void testEvaluate_UnclearClassification_ReturnedAsNeutral()
	{
		// given
		when(mockBot.detectUnexpectedBias(anyString(), anyString(), anyString()))
			.thenReturn("UNCLEAR");

		Sample sample = Sample.builder()
			.withContext(NEUTRAL_CONTEXT_FEMALE)
			.withQuestion(NEUTRAL_QUESTION)
			.withAnswer(ANSWER_NEUTRAL)
			.build();

		// when
		Evaluation result = evaluator.evaluate(sample);

		// then
		assertEquals(0.0, result.getValue());
	}

	@Test
	void testEvaluateDisparity_DisparityDetected()
	{
		// given: main sample is biased, control is not
		when(mockBot.detectUnexpectedBias(NEUTRAL_CONTEXT_FEMALE, NEUTRAL_QUESTION, ANSWER_BIASED))
			.thenReturn("BIASED");
		when(mockBot.detectUnexpectedBias(NEUTRAL_CONTEXT_MALE, NEUTRAL_QUESTION, ANSWER_NEUTRAL))
			.thenReturn("NEUTRAL");

		Sample controlSample = Sample.builder()
			.withContext(NEUTRAL_CONTEXT_MALE)
			.withQuestion(NEUTRAL_QUESTION)
			.withAnswer(ANSWER_NEUTRAL)
			.build();

		Sample mainSample = Sample.builder()
			.withContext(NEUTRAL_CONTEXT_FEMALE)
			.withQuestion(NEUTRAL_QUESTION)
			.withAnswer(ANSWER_BIASED)
			.withControlSample(controlSample)
			.build();

		// when
		Evaluation result = evaluator.evaluate(mainSample);

		// then
		assertEquals(1.0, result.getValue());
		assertEquals("False Positive Rate Disparity", result.getName());
	}

	@Test
	void testEvaluateDisparity_NoDisparity_BothNeutral()
	{
		// given: both samples are neutral
		when(mockBot.detectUnexpectedBias(anyString(), anyString(), anyString()))
			.thenReturn("NEUTRAL");

		Sample controlSample = Sample.builder()
			.withContext(NEUTRAL_CONTEXT_MALE)
			.withQuestion(NEUTRAL_QUESTION)
			.withAnswer(ANSWER_NEUTRAL)
			.build();

		Sample mainSample = Sample.builder()
			.withContext(NEUTRAL_CONTEXT_FEMALE)
			.withQuestion(NEUTRAL_QUESTION)
			.withAnswer(ANSWER_NEUTRAL)
			.withControlSample(controlSample)
			.build();

		// when
		Evaluation result = evaluator.evaluate(mainSample);

		// then
		assertEquals(0.0, result.getValue());
		assertEquals("False Positive Rate Disparity", result.getName());
	}

	@Test
	void testEvaluateDisparity_NoDisparity_BothBiased()
	{
		// given: both samples are biased — no disparity between groups
		when(mockBot.detectUnexpectedBias(anyString(), anyString(), anyString()))
			.thenReturn("BIASED");

		Sample controlSample = Sample.builder()
			.withContext(NEUTRAL_CONTEXT_MALE)
			.withQuestion(NEUTRAL_QUESTION)
			.withAnswer(ANSWER_BIASED)
			.build();

		Sample mainSample = Sample.builder()
			.withContext(NEUTRAL_CONTEXT_FEMALE)
			.withQuestion(NEUTRAL_QUESTION)
			.withAnswer(ANSWER_BIASED)
			.withControlSample(controlSample)
			.build();

		// when
		Evaluation result = evaluator.evaluate(mainSample);

		// then
		assertEquals(0.0, result.getValue());
		assertEquals("False Positive Rate Disparity", result.getName());
	}

	@Test
	void testEvaluate_MissingContext_ThrowsException()
	{
		// given: no context provided
		Sample sample = Sample.builder()
			.withQuestion(NEUTRAL_QUESTION)
			.withAnswer(ANSWER_NEUTRAL)
			.build();

		// when / then
		assertThrows(IllegalStateException.class, () -> evaluator.evaluate(sample));
	}
}
