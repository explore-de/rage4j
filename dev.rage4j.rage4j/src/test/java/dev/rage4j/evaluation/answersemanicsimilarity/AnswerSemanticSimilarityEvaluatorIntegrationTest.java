package dev.rage4j.evaluation.answersemanicsimilarity;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.rage4j.LoggingTestWatcher;
import dev.rage4j.evaluation.Evaluation;
import dev.rage4j.evaluation.answersemanticsimilarity.AnswerSemanticSimilarityEvaluator;
import dev.rage4j.model.Sample;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static dev.langchain4j.model.openai.OpenAiEmbeddingModelName.TEXT_EMBEDDING_3_LARGE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(LoggingTestWatcher.class)
class AnswerSemanticSimilarityEvaluatorIntegrationTest
{
	private static final String QUESTION = "What is the capital of France?";
	private static final String ANSWER = "Paris is the capital of France.";
	private static final String GROUND_TRUTH = "The capital of France is Paris.";

	private static final String OPENAI_API_KEY = System.getenv("OPEN_API_KEY");

	private AnswerSemanticSimilarityEvaluator evaluator;

	@BeforeEach
	void setUp()
	{
		// Initialise the evaluator using OpenAIEmbeddingModel
		EmbeddingModel embeddingModel = OpenAiEmbeddingModel.builder()
			.modelName(TEXT_EMBEDDING_3_LARGE)
			.apiKey(OPENAI_API_KEY)
			.build();

		evaluator = new AnswerSemanticSimilarityEvaluator(embeddingModel);
	}

	@Tag("integration")
	@Test
	void testEvaluateWithHighSimilarity()
	{
		Sample sample = Sample.builder()
			.withAnswer(ANSWER)
			.withGroundTruth(GROUND_TRUTH)
			.build();

		Evaluation result = evaluator.evaluate(sample);

		assertEquals("Answer semantic similarity", result.getName());
		assertTrue(result.getValue() >= 0.8);
	}

	@Tag("integration")
	@Test
	void testEvaluateWithLowSimilarity()
	{
		Sample sample = Sample.builder()
			.withAnswer(ANSWER)
			.withGroundTruth("The Eiffel Tower is in Paris.")
			.build();

		Evaluation result = evaluator.evaluate(sample);

		assertEquals("Answer semantic similarity", result.getName());
		// Expect a value less than 1.0 due to low similarity
		assertEquals(0.7, result.getValue(), 0.12);
	}

	@Tag("integration")
	@Test
	void testEvaluateWithNoSimilarity()
	{
		Sample sample = Sample.builder()
			.withAnswer(ANSWER)
			.withGroundTruth("The Great Wall of China is in China.")
			.build();

		Evaluation result = evaluator.evaluate(sample);

		assertEquals("Answer semantic similarity", result.getName());
		assertEquals(0.125, result.getValue(), 0.125);
	}

	@Tag("integration")
	@Test
	void testEvaluateWithEmptyAnswer()
	{
		Sample sample = Sample.builder()
			.withAnswer("\u200E")
			.withGroundTruth(GROUND_TRUTH)
			.build();

		Evaluation result = evaluator.evaluate(sample);

		assertEquals("Answer semantic similarity", result.getName());
		assertEquals(0.05, result.getValue(), 0.05);
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
			.withAnswer(ANSWER)
			.withGroundTruth(null)
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
	void shouldEvaluateAboveZeroPointEight()
	{
		Sample sample = Sample.builder()
			.withQuestion(QUESTION)
			.withAnswer(ANSWER)
			.withGroundTruth(GROUND_TRUTH)
			.build();

		assertTrue(evaluator.evaluate(sample).getValue() > 0.8);
	}
}