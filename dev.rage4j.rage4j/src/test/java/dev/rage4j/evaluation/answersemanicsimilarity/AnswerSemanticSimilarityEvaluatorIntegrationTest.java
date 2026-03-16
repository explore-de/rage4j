package dev.rage4j.evaluation.answersemanicsimilarity;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.rage4j.LoggingTestWatcher;
import dev.rage4j.config.ConfigFactory;
import dev.rage4j.evaluation.Evaluation;
import dev.rage4j.evaluation.answersemanticsimilarity.AnswerSemanticSimilarityEvaluator;
import dev.rage4j.model.Sample;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(LoggingTestWatcher.class)
class AnswerSemanticSimilarityEvaluatorIntegrationTest
{
	private static final String QUESTION = "What is the capital of France?";
	private static final String ANSWER = "Paris is the capital of France.";
	private static final String GROUND_TRUTH = "The capital of France is Paris.";

	private static final String OPEN_AI_KEY = ConfigFactory.getConfig().OPEN_AI_KEY();
	private static final String OPEN_AI_EMBEDDING_MODEL = ConfigFactory.getConfig().OPEN_AI_EMBEDDING_MODEL();

	private AnswerSemanticSimilarityEvaluator evaluator;

	@BeforeEach
	void setUp()
	{
		// Initialise the evaluator using OpenAIEmbeddingModel
		EmbeddingModel embeddingModel = OpenAiEmbeddingModel.builder()
			.modelName(OPEN_AI_EMBEDDING_MODEL)
			.apiKey(OPEN_AI_KEY)
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

		IllegalArgumentException exception = assertThrows(
			IllegalArgumentException.class,
			() -> evaluator.evaluate(sample));
		assertEquals("Sample must have an answer for Answer Semantic Similarity evaluation", exception.getMessage());
	}

	@Tag("integration")
	@Test
	void testEvaluateWithNullGroundTruth()
	{
		Sample sample = Sample.builder()
			.withAnswer(ANSWER)
			.withGroundTruth(null)
			.build();

		IllegalArgumentException exception = assertThrows(
			IllegalArgumentException.class,
			() -> evaluator.evaluate(sample));
		assertEquals("Sample must have a ground truth for Answer Semantic Similarity evaluation", exception.getMessage());
	}

	@Tag("integration")
	@Test
	void testEverythingCorrect()
	{
		Sample sample = Sample.builder()
			.withQuestion(QUESTION)
			.withAnswer(ANSWER)
			.withGroundTruth(GROUND_TRUTH)
			.build();

		Evaluation result = evaluator.evaluate(sample);

		assertEquals("Answer semantic similarity", result.getName());
		assertTrue(result.getValue() > 0.8);
	}
}