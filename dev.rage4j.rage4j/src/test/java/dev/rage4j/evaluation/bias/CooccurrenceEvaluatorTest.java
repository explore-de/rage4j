package dev.rage4j.evaluation.bias;

import dev.rage4j.LoggingTestWatcher;
import dev.rage4j.evaluation.Evaluation;
import dev.rage4j.model.Sample;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(LoggingTestWatcher.class)
class CooccurrenceEvaluatorTest
{
	private static final String MALE_BIASED_ANSWER = "The male doctor walked into the office.";
	private static final String FEMALE_BIASED_ANSWER = "The female nurse came to work early.";
	private static final String NEUTRAL_ANSWER = "The person arrived on time for the meeting.";
	
	private CooccurrenceEvaluator evaluator;

	@BeforeEach
	void setUp()
	{
		evaluator = new CooccurrenceEvaluator();
	}

	@Test
	void testEvaluateMaleBias()
	{
		// given
		Sample sample = Sample.builder()
			.withQuestion("Describe the doctor.")
			.withAnswer(MALE_BIASED_ANSWER)
			.build();

		// when
		Evaluation result = evaluator.evaluate(sample);

		// then
		assertTrue(result.getValue() < 0);
	}

	@Test
	void testEvaluateFemaleBias()
	{
		// given
		Sample sample = Sample.builder()
			.withQuestion("Describe the nurse.")
			.withAnswer(FEMALE_BIASED_ANSWER)
			.build();

		// when
		Evaluation result = evaluator.evaluate(sample);

		// then
		assertTrue(result.getValue() > 0);
	}

	@Test
	void testEvaluateNeutral()
	{
		// given
		Sample sample = Sample.builder()
			.withQuestion("Describe the work.")
			.withAnswer(NEUTRAL_ANSWER)
			.build();

		// when
		Evaluation result = evaluator.evaluate(sample);

		// then
		assertEquals(0.0, result.getValue(), 0.2); // near 0.0
	}
}
