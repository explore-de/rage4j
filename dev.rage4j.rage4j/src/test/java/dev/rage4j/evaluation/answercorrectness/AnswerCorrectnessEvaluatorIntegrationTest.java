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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(LoggingTestWatcher.class)
class AnswerCorrectnessEvaluatorIntegrationTest
{
	private static final String GROUND_TRUTH = "Paris";
	private static final String ANSWER_CORRECT = "Paris is the capital of France.";
	private static final String ANSWER_WITH_FALSE_POSITIVE = "Paris is the capital of France and the largest city in France.";
	private static final String ANSWER_WITH_FALSE_NEGATIVE = "Paris is the capital.";
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
			.withAnswer(GROUND_TRUTH + " is the capital.")
			.build();

		Evaluation result = evaluator.evaluate(sample);

		assertEquals("Answer correctness", result.getName());
		assertEquals(0.66, result.getValue(), 0.01);
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
		// Expect a value less than 1.0 due to false positive
		assertEquals(0.5, result.getValue(), 0.1);
	}

	@Tag("integration")
	@Test
	void testEvaluateWithFalseNegative()
	{
		Sample sample = Sample.builder()
			.withGroundTruth(GROUND_TRUTH + " is the capital of France.")
			.withAnswer(ANSWER_WITH_FALSE_NEGATIVE)
			.build();

		Evaluation result = evaluator.evaluate(sample);

		assertEquals("Answer correctness", result.getName());
		// Expect a value less than 1.0 due to false negative
		assertEquals(0.5, result.getValue(), 0.001);
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
			() -> evaluator.evaluate(sample)
		);
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
			() -> evaluator.evaluate(sample)
		);
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