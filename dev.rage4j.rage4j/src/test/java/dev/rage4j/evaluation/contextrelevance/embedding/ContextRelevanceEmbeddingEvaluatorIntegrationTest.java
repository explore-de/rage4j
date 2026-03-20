package dev.rage4j.evaluation.contextrelevance.embedding;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.rage4j.LoggingTestWatcher;
import dev.rage4j.config.ConfigFactory;
import dev.rage4j.evaluation.Evaluation;
import dev.rage4j.model.Sample;
import dev.rage4j.util.StringSimilarityBatchComputer;
import dev.rage4j.util.StringSimilarityComputer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(LoggingTestWatcher.class)
class ContextRelevanceEmbeddingEvaluatorIntegrationTest
{
	private static final String QUESTION = "What is the capital of France?";
	private static final String RELEVANT_CONTEXT = "Paris is the capital of France.";
	private static final String IRRELEVANT_CONTEXT = "The Great Wall of China is located in northern China.";

	private static final String OPEN_AI_KEY = ConfigFactory.getConfig().OPEN_AI_KEY();
	private static final String OPEN_AI_EMBEDDING_MODEL = ConfigFactory.getConfig().OPEN_AI_EMBEDDING_MODEL();

	private ContextRelevanceEmbeddingEvaluator evaluator;

	@BeforeEach
	void setUp()
	{
		EmbeddingModel embeddingModel = OpenAiEmbeddingModel.builder()
			.modelName(OPEN_AI_EMBEDDING_MODEL)
			.apiKey(OPEN_AI_KEY)
			.build();

		evaluator = new ContextRelevanceEmbeddingEvaluator(new StringSimilarityBatchComputer(embeddingModel));
	}

	@Tag("integration")
	@Test
	void testEvaluateWithHighRelevance()
	{
		Sample sample = Sample.builder()
			.withQuestion(QUESTION)
			.withContext(RELEVANT_CONTEXT)
			.build();

		Evaluation result = evaluator.evaluate(sample);

		assertEquals("context relevance embedding", result.getName());
		assertTrue(result.getValue() >= 0.6);
	}

	@Tag("integration")
	@Test
	void testEvaluateWithLowRelevance()
	{
		Sample sample = Sample.builder()
			.withQuestion(QUESTION)
			.withContext(IRRELEVANT_CONTEXT)
			.build();

		Evaluation result = evaluator.evaluate(sample);

		assertEquals("context relevance embedding", result.getName());
		assertTrue(result.getValue() < 0.5);
	}

	@Tag("integration")
	@Test
	void testEvaluateWithRelevantAndIrrelevantChunks()
	{
		String multiChunkContext = RELEVANT_CONTEXT + "\n\n" + IRRELEVANT_CONTEXT;

		Sample sample = Sample.builder()
			.withQuestion(QUESTION)
			.withContext(multiChunkContext)
			.build();

		Evaluation result = evaluator.evaluate(sample);

		assertEquals("context relevance embedding", result.getName());
		// Best chunk (relevant one) should yield a high score
		assertTrue(result.getValue() <= 0.5);
	}

	@Tag("integration")
	@Test
	void testEvaluateWithNullQuestion()
	{
		Sample sample = Sample.builder()
			.withQuestion(null)
			.withContext(RELEVANT_CONTEXT)
			.build();

		IllegalArgumentException exception = assertThrows(
			IllegalArgumentException.class,
			() -> evaluator.evaluate(sample));
		assertEquals("Sample must have a question for context relevance embedding evaluation", exception.getMessage());
	}

	@Tag("integration")
	@Test
	void testEvaluateWithNullContext()
	{
		Sample sample = Sample.builder()
			.withQuestion(QUESTION)
			.withContext(null)
			.build();

		IllegalArgumentException exception = assertThrows(
			IllegalArgumentException.class,
			() -> evaluator.evaluate(sample));
		assertEquals("Sample must have context for context relevance embedding evaluation", exception.getMessage());
	}
}
