package dev.rage4j.evaluation.faithfulness;

import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.rage4j.LoggingTestWatcher;
import dev.rage4j.evaluation.Evaluation;
import dev.rage4j.model.Sample;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(LoggingTestWatcher.class)
class FaithfulnessEvaluatorIntegrationTest
{
	private static final String QUESTION = "What is the capital of France?";
	private static final String ANSWER = "Paris is the capital of France.";
	private static final String GROUND_TRUTH = "Paris";
	private static final List<String> CONTEXTS = List.of("Paris is the capital of France.");

	private static final String OPENAI_API_KEY = System.getenv("OPENAI_API_KEY");

	private FaithfulnessEvaluator evaluator;

	@BeforeEach
	void setUp()
	{
		// Initialise the evaluator using OpenAIChatModel
		OpenAiChatModel model = OpenAiChatModel.builder()
			.apiKey(OPENAI_API_KEY)
			.modelName(GPT_4_O)
			.build();

		evaluator = new FaithfulnessEvaluator(model);
	}

	@Tag("integration")
	@Test
	void testEvaluateFaithfulnessFullMatch()
	{
		Sample sample = Sample.builder()
			.withQuestion(QUESTION)
			.withAnswer(ANSWER)
			.withGroundTruth(GROUND_TRUTH)
			.withContextsList(CONTEXTS)
			.build();

		Evaluation result = evaluator.evaluate(sample);

		assertEquals("Faithfulness", result.getName());
		assertEquals(1.0, result.getValue());
	}

	@Tag("integration")
	@Test
	void testEvaluateFaithfulnessPartialMatch()
	{
		Sample sample = Sample.builder()
			.withQuestion(QUESTION)
			.withAnswer("Paris is the capital of France. London is the capital of England.")
			.withGroundTruth(GROUND_TRUTH)
			.withContextsList(CONTEXTS)
			.build();

		Evaluation result = evaluator.evaluate(sample);

		assertEquals("Faithfulness", result.getName());
		// Expect a value less than 1.0 due to partial match
		assertEquals(0.5, result.getValue());
	}

	@Tag("integration")
	@Test
	void testEvaluateFaithfulnessNoMatch()
	{
		Sample sample = Sample.builder()
			.withQuestion(QUESTION)
			.withAnswer("London is the capital of England. Berlin is the capital of Germany.")
			.withGroundTruth(GROUND_TRUTH)
			.withContextsList(CONTEXTS)
			.build();

		Evaluation result = evaluator.evaluate(sample);

		assertEquals("Faithfulness", result.getName());
		assertEquals(0.0, result.getValue());
	}

	@Tag("integration")
	@Test
	void testEvaluateFaithfulnessNullContext()
	{
		Sample sample = Sample.builder()
			.withQuestion(QUESTION)
			.withAnswer(ANSWER)
			.withGroundTruth(GROUND_TRUTH)
			.withContextsList(null)
			.build();

		try
		{
			evaluator.evaluate(sample);
		}
		catch (IllegalStateException e)
		{
			assertEquals("Attribute not found: contextsList", e.getMessage());
		}
	}

	@Tag("integration")
	@Test
	void testEvaluateFaithfulnessNullAnswer()
	{
		Sample sample = Sample.builder()
			.withQuestion(QUESTION)
			.withAnswer(null)
			.withGroundTruth(GROUND_TRUTH)
			.withContextsList(CONTEXTS)
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
	void shouldEvaluateAboveZeroPointEight()
	{
		Sample sample = Sample.builder()
			.withQuestion(QUESTION)
			.withAnswer(ANSWER)
			.withGroundTruth(GROUND_TRUTH)
			.withContextsList(CONTEXTS)
			.build();

		assertTrue(evaluator.evaluate(sample).getValue() > 0.8);
	}
}