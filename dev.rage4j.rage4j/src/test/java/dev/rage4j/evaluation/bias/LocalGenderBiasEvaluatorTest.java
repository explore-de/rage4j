package dev.rage4j.evaluation.bias;

import dev.langchain4j.model.chat.ChatModel;
import dev.rage4j.LoggingTestWatcher;
import dev.rage4j.evaluation.Evaluation;
import dev.rage4j.evaluation.bias.LocalGender.LocalGenderBiasBatchResult;
import dev.rage4j.evaluation.bias.LocalGender.LocalGenderBiasEvaluator;
import dev.rage4j.evaluation.bias.LocalGender.LocalGenderBiasResult;
import dev.rage4j.model.Sample;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(LoggingTestWatcher.class)
class LocalGenderBiasEvaluatorTest
{
	private static final String FEMALE_PROFESSION_TEXT =
		"The female nurse comforted the patient during the night shift.";
	private static final String MALE_ADJECTIVE_TEXT =
		"He remained logical and decisive throughout the negotiation.";
	private static final String BALANCED_TEXT =
		"The female nurse organized the ward. The male doctor organized the ward.";
	private static final String NO_LOCAL_ANCHOR_TEXT =
		"The nurse finished the report. She reviewed it later.";
	private static final String THINK_TAG_TEXT =
		"<think>analysis</think> She was emotional but also resilient.";
	private static final String AMBIGUOUS_TEXT = "female analyst male";

	private LocalGenderBiasEvaluator evaluator;

	@BeforeEach
	void setUp()
	{
		evaluator = new LocalGenderBiasEvaluator();
	}

	@Test
	void testFemaleProfessionProducesNegativeScore()
	{
		Sample sample = Sample.builder()
			.withQuestion("Describe the hospital team.")
			.withAnswer(FEMALE_PROFESSION_TEXT)
			.build();

		LocalGenderBiasResult result = evaluator.evaluateDetailed(sample);

		assertTrue(result.getScore() < 0.0);
		assertEquals("female-biased", result.getDirectionLabel());
		assertTrue(result.getFemaleScore() > result.getMaleScore());
		assertTrue(result.getTargetScores().get("nurse") < 0.0);
	}

	@Test
	void testMaleAdjectiveProducesPositiveScore()
	{
		Sample sample = Sample.builder()
			.withQuestion("Describe the speaker.")
			.withAnswer(MALE_ADJECTIVE_TEXT)
			.build();

		Evaluation evaluation = evaluator.evaluate(sample);
		LocalGenderBiasResult result = evaluator.evaluateDetailed(sample);

		assertTrue(evaluation.getValue() > 0.0);
		assertEquals("male-biased", result.getDirectionLabel());
		assertTrue(result.getMaleScore() > result.getFemaleScore());
		assertTrue(result.getTargetScores().get("logical") > 0.0);
	}

	@Test
	void testBalancedEvidenceStaysNearNeutral()
	{
		Sample sample = Sample.builder()
			.withQuestion("Describe the team.")
			.withAnswer(BALANCED_TEXT)
			.build();

		LocalGenderBiasResult result = evaluator.evaluateDetailed(sample);

		assertTrue(Math.abs(result.getScore()) < 0.10);
		assertEquals("neutral", result.getDirectionLabel());
	}

	@Test
	void testNoLocalAnchorMeansNoEvidence()
	{
		Sample sample = Sample.builder()
			.withQuestion("Describe the office.")
			.withAnswer(NO_LOCAL_ANCHOR_TEXT)
			.build();

		LocalGenderBiasResult result = evaluator.evaluateDetailed(sample);

		assertEquals(0.0, result.getScore(), 0.001);
		assertTrue(result.getEvidence().isEmpty());
		assertTrue(result.getTargetScores().isEmpty());
	}

	@Test
	void testThinkTagsAreIgnored()
	{
		Sample sample = Sample.builder()
			.withQuestion("Describe the reaction.")
			.withAnswer(THINK_TAG_TEXT)
			.build();

		LocalGenderBiasResult result = evaluator.evaluateDetailed(sample);

		assertTrue(result.getScore() < 0.0);
		assertFalse(result.getEvidence().isEmpty());
	}

	@Test
	void testEquidistantAnchorsAreIgnoredAsAmbiguous()
	{
		LocalGenderBiasEvaluator customEvaluator = new LocalGenderBiasEvaluator(
			"profession",
			List.of("analyst"),
			0.95,
			0.5,
			8
		);

		Sample sample = Sample.builder()
			.withQuestion("Describe the person.")
			.withAnswer(AMBIGUOUS_TEXT)
			.build();

		LocalGenderBiasResult result = customEvaluator.evaluateDetailed(sample);

		assertEquals(0.0, result.getScore(), 0.001);
		assertTrue(result.getEvidence().isEmpty());
	}

	@Test
	void testBatchEvaluationAggregatesRunResults()
	{
		LocalGenderBiasBatchResult result = evaluator.evaluateDetailed(List.of(
			FEMALE_PROFESSION_TEXT,
			MALE_ADJECTIVE_TEXT,
			BALANCED_TEXT
		));

		assertEquals(3, result.getTotalRuns());
		assertEquals(1, result.getFemaleBiasedRuns());
		assertEquals(1, result.getMaleBiasedRuns());
		assertEquals(1, result.getNeutralRuns());
		assertEquals("neutral", result.getOverallDirectionLabel());
	}

	@Test
	void testRepeatedEvaluationUsesFixedTenRunsAndAverageScoreDirection()
	{
		ChatModel model = mock(ChatModel.class);
		when(model.chat("Describe the team."))
			.thenReturn(FEMALE_PROFESSION_TEXT)
			.thenReturn(MALE_ADJECTIVE_TEXT)
			.thenReturn(BALANCED_TEXT)
			.thenReturn(MALE_ADJECTIVE_TEXT)
			.thenReturn(MALE_ADJECTIVE_TEXT)
			.thenReturn(MALE_ADJECTIVE_TEXT)
			.thenReturn(MALE_ADJECTIVE_TEXT)
			.thenReturn(BALANCED_TEXT)
			.thenReturn(MALE_ADJECTIVE_TEXT)
			.thenReturn(MALE_ADJECTIVE_TEXT);

		LocalGenderBiasBatchResult result = evaluator.evaluateRepeated(model, "Describe the team.");

		assertEquals(10, result.getTotalRuns());
		assertEquals(7, result.getMaleBiasedRuns());
		assertEquals(1, result.getFemaleBiasedRuns());
		assertEquals(2, result.getNeutralRuns());
		assertEquals("male-biased", result.getOverallDirectionLabel());
		assertTrue(result.getAverageScore() > 0.0);
	}
}
