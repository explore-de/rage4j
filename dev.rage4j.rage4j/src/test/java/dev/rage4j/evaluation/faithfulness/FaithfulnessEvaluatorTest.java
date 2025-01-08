package dev.rage4j.evaluation.faithfulness;

import dev.rage4j.LoggingTestWatcher;
import dev.rage4j.evaluation.Evaluation;
import dev.rage4j.evaluation.model.ArrayResponse;
import dev.rage4j.model.Sample;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(LoggingTestWatcher.class)
class FaithfulnessEvaluatorTest
{
	private static final String QUESTION = "What is the capital of France?";
	private static final String ANSWER = "Paris is the capital of France.";
	private static final String GROUND_TRUTH = "Paris";
	private static final List<String> CONTEXTS = List.of("Paris is the capital of France.");
	private static final double EXPECTED_SCORE_FULL_MATCH = 1.0;
	private static final double EXPECTED_SCORE_PARTIAL_MATCH = 0.5;
	private static final double EXPECTED_SCORE_NO_MATCH = 0.0;

	private FaithfulnessEvaluator evaluator;
	private FaithfulnessBot mockBot;
	private Sample sample;

	@BeforeEach
	void setUp()
	{
		mockBot = mock(FaithfulnessBot.class);
		evaluator = new FaithfulnessEvaluator(mockBot);
		sample = Sample.builder()
			.withQuestion(QUESTION)
			.withAnswer(ANSWER)
			.withGroundTruth(GROUND_TRUTH)
			.withContextsList(CONTEXTS)
			.build();
	}

	@Test
	void testEvaluateFaithfulness_FullMatch()
	{
		when(mockBot.extractClaims(ANSWER)).thenReturn(new ArrayResponse(new String[] { "Paris" }));
		when(mockBot.canBeInferred(anyString(), eq("Paris"))).thenReturn(true);

		Evaluation result = evaluator.evaluate(sample);

		assertEquals(EXPECTED_SCORE_FULL_MATCH, result.getValue());
		assertEquals("Faithfulness", result.getName());
	}

	@Test
	void testEvaluateFaithfulness_PartialMatch()
	{
		when(mockBot.extractClaims(ANSWER)).thenReturn(new ArrayResponse(new String[] { "Paris", "London" }));
		when(mockBot.canBeInferred(anyString(), eq("Paris"))).thenReturn(true);
		when(mockBot.canBeInferred(anyString(), eq("London"))).thenReturn(false);

		Evaluation result = evaluator.evaluate(sample);

		assertEquals(EXPECTED_SCORE_PARTIAL_MATCH, result.getValue());
		assertEquals("Faithfulness", result.getName());
	}

	@Test
	void testEvaluateFaithfulness_NoMatch()
	{
		when(mockBot.extractClaims(ANSWER)).thenReturn(new ArrayResponse(new String[] { "London", "Berlin" }));
		when(mockBot.canBeInferred(anyString(), eq("London"))).thenReturn(false);
		when(mockBot.canBeInferred(anyString(), eq("Berlin"))).thenReturn(false);

		Evaluation result = evaluator.evaluate(sample);

		assertEquals(EXPECTED_SCORE_NO_MATCH, result.getValue());
		assertEquals("Faithfulness", result.getName());
	}

	@Test
	void testEvaluateFaithfulnessEmptyClaims()
	{
		when(mockBot.extractClaims(ANSWER)).thenReturn(new ArrayResponse(new String[] {}));

		Evaluation result = evaluator.evaluate(sample);

		assertEquals(EXPECTED_SCORE_NO_MATCH, result.getValue());
		assertEquals("Faithfulness", result.getName());
	}

	@Test
	void testEvaluateFaithfulnessNullContext()
	{
		Sample nullContextSample = Sample.builder()
			.withQuestion(QUESTION)
			.withAnswer(ANSWER)
			.withGroundTruth(GROUND_TRUTH)
			.withContextsList(null)
			.build();

		IllegalStateException exception = assertThrows(IllegalStateException.class, () -> evaluator.evaluate(nullContextSample));
		assertEquals("Attribute not found: contextsList", exception.getMessage());
	}

	@Test
	void testEvaluateFaithfulnessNullAnswer()
	{
		Sample nullAnswerSample = Sample.builder()
			.withQuestion(QUESTION)
			.withAnswer(null)
			.withGroundTruth(GROUND_TRUTH)
			.withContextsList(CONTEXTS)
			.build();

		IllegalStateException exception = assertThrows(IllegalStateException.class, () -> evaluator.evaluate(nullAnswerSample));
		assertEquals("Attribute not found: answer", exception.getMessage());
	}
}
