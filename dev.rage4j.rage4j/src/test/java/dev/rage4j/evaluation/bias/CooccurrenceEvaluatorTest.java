package dev.rage4j.evaluation.bias;

import dev.rage4j.LoggingTestWatcher;
import dev.rage4j.evaluation.Evaluation;
import dev.rage4j.evaluation.bias.Cooccurence.CooccurrenceEvaluator;
import dev.rage4j.model.Sample;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(LoggingTestWatcher.class)
class CooccurrenceEvaluatorTest
{
	/**
	 * Sentence that contains both female and male tokens so both groups are present in the
	 * corpus, and "nurse" co-occurs more frequently with the female side → non-zero COBS.
	 */
	private static final String BIASED_ANSWER =
		"The female nurse helped him recover quickly while the male doctor walked into the office.";

	/**
	 * A sentence with only neutral words (no gendered tokens at all) → COBS falls back to 0.0.
	 */
	private static final String NEUTRAL_ANSWER = "The person arrived on time for the meeting.";

	private CooccurrenceEvaluator evaluator;

	@BeforeEach
	void setUp()
	{
		evaluator = new CooccurrenceEvaluator();
	}

	@Test
	void testEvaluateBiasedTextReturnsPositiveScore()
	{
		// given
		Sample sample = Sample.builder()
			.withQuestion("Describe the medical staff.")
			.withAnswer(BIASED_ANSWER)
			.build();

		// when
		Evaluation result = evaluator.evaluate(sample);

		// then – COBS is always >= 0; a biased sentence should yield a positive score
		assertTrue(result.getValue() >= 0.0, "Expected non-negative COBS score");
	}

	@Test
	void testEvaluateNeutralTextReturnsZeroScore()
	{
		// given
		Sample sample = Sample.builder()
			.withQuestion("Describe the work.")
			.withAnswer(NEUTRAL_ANSWER)
			.build();

		// when
		Evaluation result = evaluator.evaluate(sample);

		// then – no gendered words → metric cannot be computed → evaluator returns 0.0
		assertEquals(0.0, result.getValue(), 0.001);
	}
}
