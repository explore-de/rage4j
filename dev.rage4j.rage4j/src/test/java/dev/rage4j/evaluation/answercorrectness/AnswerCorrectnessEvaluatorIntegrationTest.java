package dev.rage4j.evaluation.answercorrectness;

import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.rage4j.LoggingTestWatcher;
import dev.rage4j.evaluation.Evaluation;
import dev.rage4j.model.Sample;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(LoggingTestWatcher.class)
class AnswerCorrectnessEvaluatorIntegrationTest
{
	private static final String GROUND_TRUTH = "Paris";
	private static final String ANSWER_CORRECT = "Paris is the capital of France.";
	private static final String ANSWER_WITH_FALSE_POSITIVE = "Paris is the capital of France and the largest city in France.";
	private static final String ANSWER_WITH_FALSE_NEGATIVE = "Paris is the capital.";
	private static final String QUESTION = "What is the capital of France?";

	private static final String OPEN_AI_KEY = System.getenv("OPEN_AI_KEY");

	private AnswerCorrectnessEvaluator evaluator;

	@BeforeEach
	void setUp()
	{
		// Initialize the evaluator using OpenAIChatModel
		OpenAiChatModel model = OpenAiChatModel.builder()
			.apiKey(OPEN_AI_KEY)
			.modelName(GPT_4)
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

		try
		{
			evaluator.evaluate(sample);
		}
		catch (IllegalStateException e)
		{
			assertEquals("Attribute not found: answer", e.getMessage());
		}
	}

	@Tag("integration")
	@Test
	void testEvaluateWithNullGroundTruth()
	{
		Sample sample = Sample.builder()
			.withGroundTruth(null)
			.withAnswer(ANSWER_CORRECT)
			.build();

		try
		{
			evaluator.evaluate(sample);
		}
		catch (IllegalStateException e)
		{
			assertEquals("Attribute not found: groundTruth", e.getMessage());
		}
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