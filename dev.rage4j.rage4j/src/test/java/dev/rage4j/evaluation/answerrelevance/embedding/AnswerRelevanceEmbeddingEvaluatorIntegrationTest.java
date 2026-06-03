package dev.rage4j.evaluation.answerrelevance.embedding;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.rage4j.LoggingTestWatcher;
import dev.rage4j.config.ConfigFactory;
import dev.rage4j.evaluation.Evaluation;
import dev.rage4j.model.Sample;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(LoggingTestWatcher.class)
class AnswerRelevanceEmbeddingEvaluatorIntegrationTest
{

	private static final String QUESTION = "What is the capital of France?";
	private static final String ANSWER = "Paris is the capital of France.";
	private static final String GROUND_TRUTH = "Paris";

	private static final String OPEN_AI_KEY = ConfigFactory.getConfig().OPEN_AI_KEY();
	private static final String OPEN_AI_MODEL = ConfigFactory.getConfig().OPEN_AI_MODEL();
	private static final String OPEN_AI_EMBEDDING_MODEL = ConfigFactory.getConfig().OPEN_AI_EMBEDDING_MODEL();

	private AnswerRelevanceEmbeddingEvaluator evaluator;

	@BeforeEach
	void setUp()
	{
		// Initialise the evaluator using OpenAIChatModel and
		// OpenAIEmbeddingModel
		OpenAiChatModel chatModel = OpenAiChatModel.builder()
			.apiKey(OPEN_AI_KEY)
			.modelName(OPEN_AI_MODEL)
			.responseFormat("json_object")
			.build();

		EmbeddingModel embeddingModel = OpenAiEmbeddingModel.builder()
			.modelName(OPEN_AI_EMBEDDING_MODEL)
			.apiKey(OPEN_AI_KEY)
			.build();

		evaluator = new AnswerRelevanceEmbeddingEvaluator(chatModel, embeddingModel);
	}

	@Tag("integration")
	@Test
	void testEvaluateRelevanceEmbeddingFullSimilarity()
	{
		Sample sample = Sample.builder()
			.withQuestion(QUESTION)
			.withAnswer(ANSWER)
			.build();

		Evaluation result = evaluator.evaluate(sample);

		assertEquals("Answer relevance embedding", result.getName());
		assertEquals(0.875, result.getValue(), 0.125);
	}

	@Tag("integration")
	@Test
	void testEvaluateRelevanceEmbeddingWithPartialSimilarity()
	{
		Sample sample = Sample.builder()
			.withQuestion(QUESTION)
			.withAnswer("Paris is the capital of France, known for the Eiffel Tower.")
			.build();

		Evaluation result = evaluator.evaluate(sample);

		assertTrue(result.getValue() >= 0.5);
		assertEquals("Answer relevance embedding", result.getName());

		// Expect a value less than 1.0 due to partial similarity
		assertTrue(result.getValue() < 1.0);
	}

	@Tag("integration")
	@Test
	void testEvaluateRelevanceEmbeddingWithEmptyAnswer()
	{
		Sample sample = Sample.builder()
			.withQuestion(QUESTION)
			.withAnswer("")
			.build();

		Evaluation result = evaluator.evaluate(sample);

		assertEquals("Answer relevance embedding", result.getName());
		assertEquals(0.05, result.getValue(), 0.05);
	}

	@Tag("integration")
	@Test
	void testEvaluateRelevanceEmbeddingWithNullAnswer()
	{
		Sample sample = Sample.builder()
			.withQuestion(QUESTION)
			.withAnswer(null)
			.build();

		IllegalArgumentException exception = assertThrows(
			IllegalArgumentException.class,
			() -> evaluator.evaluate(sample));
		assertEquals("Sample must have an answer for answer relevance embedding evaluation", exception.getMessage());
	}

	@Tag("integration")
	@Test
	void testEvaluateRelevanceEmbeddingWithNullQuestion()
	{
		Sample sample = Sample.builder()
			.withAnswer(ANSWER)
			.withQuestion(null)
			.build();

		IllegalArgumentException exception = assertThrows(
			IllegalArgumentException.class,
			() -> evaluator.evaluate(sample));
		assertEquals("Sample must have a question for answer relevance embedding evaluation", exception.getMessage());
	}

	@Tag("integration")
	@Test
	void testEverythingEmbeddingCorrect()
	{
		Sample sample = Sample.builder()
			.withQuestion(QUESTION)
			.withAnswer(ANSWER)
			.withGroundTruth(GROUND_TRUTH)
			.build();

		Evaluation result = evaluator.evaluate(sample);

		assertEquals("Answer relevance embedding", result.getName());
		assertEquals(0.8, result.getValue(), 0.1);
	}
}