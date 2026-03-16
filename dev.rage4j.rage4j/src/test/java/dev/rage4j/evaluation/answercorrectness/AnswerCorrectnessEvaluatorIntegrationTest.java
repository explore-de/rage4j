package dev.rage4j.evaluation.answercorrectness;

import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.rage4j.LoggingTestWatcher;
import dev.rage4j.config.ConfigFactory;
import dev.rage4j.evaluation.Evaluation;
import dev.rage4j.model.Sample;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(LoggingTestWatcher.class)
class AnswerCorrectnessEvaluatorIntegrationTest
{
	private static final Logger LOG = LoggerFactory.getLogger(AnswerCorrectnessEvaluatorIntegrationTest.class);

	private static final String GROUND_TRUTH = "Paris is the capital of France.";
	private static final String ANSWER_CORRECT = "Paris is the capital of France.";
	private static final String ANSWER_WITH_FALSE_POSITIVE = "Paris is the capital of Germany";
	private static final String ANSWER_WITH_FALSE_NEGATIVE = "Paris is the capital.";

	// Tests where more than one claim is extracted:
	// Question could be here: "Which city is the capital of France and which is the largest city in France?"
	private static final String GROUND_TRUTH_ENHANCED = "Paris is the capital and the largest city in France.";
	private static final String ANSWER_WITH_FALSE_POSITIVE_ENHANCED = "Paris is the capital of Germany and France and the largest city in France.";
	private static final String ANSWER_WITH_FALSE_NEGATIVE_ENHANCED = "Paris is the capital of France.";


	private static final String QUESTION = "What is the capital of France?";

	private static final String OPEN_AI_KEY = ConfigFactory.getConfig().OPEN_AI_KEY();
	private static final String OPEN_AI_MODEL = ConfigFactory.getConfig().OPEN_AI_MODEL();

	private AnswerCorrectnessEvaluator evaluator;

	@BeforeEach
	void setUp()
	{
		// Initialize the evaluator using OpenAIChatModel
		OpenAiChatModel model = OpenAiChatModel.builder()
			.apiKey(OPEN_AI_KEY)
			.modelName(OPEN_AI_MODEL)
			.build();
		evaluator = new AnswerCorrectnessEvaluator(model);
	}

	@Tag("integration")
	@Test
	void testEvaluateWithCorrectAnswer()
	{
		Sample sample = Sample.builder()
			.withGroundTruth(GROUND_TRUTH)
			.withAnswer(GROUND_TRUTH)
			.build();

		Evaluation result = evaluator.evaluate(sample);

		assertEquals("Answer correctness", result.getName());
		assertEquals(1, result.getValue(), 0.01);
	}

	@Tag("integration")
	@Test
	void testEvaluateWithFalsePositive()
	{
		Sample sample = Sample.builder()
			.withGroundTruth(GROUND_TRUTH)
			.withAnswer(ANSWER_WITH_FALSE_POSITIVE)
			.build();

		Evaluation result = evaluator.evaluate(sample);

		assertEquals("Answer correctness", result.getName());
		// Expect a score of 0.0 because the answer contradicts the ground truth,
		// leading to no true positives (and both a false positive and a false negative).
		assertEquals(0.0, result.getValue(), 0.1);
	}

	@Tag("integration")
	@Test
	void testEvaluateWithFalsePositiveEnhanced()
	{
		Sample sample = Sample.builder()
				.withGroundTruth(GROUND_TRUTH_ENHANCED)
				.withAnswer(ANSWER_WITH_FALSE_POSITIVE_ENHANCED)
				.build();

		Evaluation result = evaluator.evaluate(sample);

		assertEquals("Answer correctness", result.getName());
		// Expect a value less than 1.0 due to
		// - 2 TP: "Paris is the capital of France" and "Paris is the largest City in France"
		// - 1 FP: "Paris is the capital of Germany"
		assertEquals(0.7, result.getValue(), 0.1);
	}

	@Tag("integration")
	@Test
	void testEvaluateWithFalseNegative()
	{
		Sample sample = Sample.builder()
			.withGroundTruth(GROUND_TRUTH)
			.withAnswer(ANSWER_WITH_FALSE_NEGATIVE)
			.build();

		Evaluation result = evaluator.evaluate(sample);

		assertEquals("Answer correctness", result.getName());
		// Expect a value of 0.0 due to:
		// - No TP: because Answer and/or GT is too short see testEvaluateWithFalseNegativeEnhanced() -> could be 1 TP or 0 TP
		// - FN: "...of France"
		var success =
				result.getValue() >= 0 && result.getValue() <= 0.1 || // no TP extracted
				result.getValue() >= 0.6 && result.getValue() <= 0.7; // TP extracted
		assertTrue(success);
	}

	@Tag("integration")
	@Test
	void testEvaluateWithFalseNegativeEnhanced()
	{
		Sample sample = Sample.builder()
				.withGroundTruth(GROUND_TRUTH_ENHANCED)
				.withAnswer(ANSWER_WITH_FALSE_NEGATIVE_ENHANCED)
				.build();

		Evaluation result = evaluator.evaluate(sample);

		assertEquals("Answer correctness", result.getName());
		// Expect a value less than 0.6 due to:
		// - TP: "Paris is the Capital of France"
		// - FN: "...of France"
		//
        LOG.info("Result: {}", result.getValue());
		assertEquals(0.66, result.getValue(), 0.1);
	}

	@Tag("integration")
	@Test
	void testEvaluateWithEmptyAnswer()
	{
		Sample sample = Sample.builder()
			.withGroundTruth(GROUND_TRUTH)
			.withAnswer("")
			.build();

		Evaluation result = evaluator.evaluate(sample);

		assertEquals("Answer correctness", result.getName());
		assertEquals(0.0, result.getValue(), 0.001);
	}

	@Tag("integration")
	@Test
	void testEvaluateWithNullAnswer()
	{
		Sample sample = Sample.builder()
			.withGroundTruth(GROUND_TRUTH)
			.withAnswer(null)
			.build();

		IllegalArgumentException exception = assertThrows(
			IllegalArgumentException.class,
			() -> evaluator.evaluate(sample));
		assertEquals("Sample must have an answer for Answer Correctness evaluation", exception.getMessage());
	}

	@Tag("integration")
	@Test
	void testEvaluateWithNullGroundTruth()
	{
		Sample sample = Sample.builder()
			.withGroundTruth(null)
			.withAnswer(ANSWER_CORRECT)
			.build();

		IllegalArgumentException exception = assertThrows(
			IllegalArgumentException.class,
			() -> evaluator.evaluate(sample));
		assertEquals("Sample must have a ground truth for Answer Correctness evaluation", exception.getMessage());
	}

	@Tag("integration")
	@Test
	void testEverythingCorrect()
	{
		Sample sample = Sample.builder()
			.withQuestion(QUESTION)
			.withAnswer(ANSWER_CORRECT)
			.withGroundTruth(ANSWER_CORRECT)
			.build();

		assertEquals(1.0, evaluator.evaluate(sample).getValue(), 0.1);
	}
}