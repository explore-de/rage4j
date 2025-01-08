package dev.rage4j.evaluation.answerrelevance;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.rage4j.LoggingTestWatcher;
import dev.rage4j.evaluation.Evaluation;
import dev.rage4j.model.Sample;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.function.BiFunction;

import static dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O;
import static dev.langchain4j.model.openai.OpenAiEmbeddingModelName.TEXT_EMBEDDING_3_LARGE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(LoggingTestWatcher.class)
class AnswerRelevanceEvaluatorIntegrationTest
{

	private static final String QUESTION = "What is the capital of France?";
	private static final String ANSWER = "Paris is the capital of France.";
	private static final String GROUND_TRUTH = "Paris";

	private static final String OPENAI_API_KEY = System.getenv("OPENAI_API_KEY");

	private AnswerRelevanceEvaluator evaluator;
	private AnswerRelevanceBot mockBot;
	private BiFunction<String, String, Double> mockStringSimilarityComputer;

	@BeforeEach
	void setUp()
	{
		// Initialise the evaluator using OpenAIChatModel and
		// OpenAIEmbeddingModel
		OpenAiChatModel chatModel = OpenAiChatModel.builder()
			.apiKey(OPENAI_API_KEY)
			.modelName(GPT_4_O)
			.responseFormat("json_object")
			.build();

		EmbeddingModel embeddingModel = OpenAiEmbeddingModel.builder()
			.modelName(TEXT_EMBEDDING_3_LARGE)
			.apiKey(OPENAI_API_KEY)
			.build();

		evaluator = new AnswerRelevanceEvaluator(chatModel, embeddingModel);
	}

	@Tag("integration")
	@Test
	void testEvaluateRelevanceFullSimilarity()
	{
		Sample sample = Sample.builder()
			.withQuestion(QUESTION)
			.withAnswer(ANSWER)
			.build();

		Evaluation result = evaluator.evaluate(sample);

		assertEquals("Answer relevance", result.getName());
		assertEquals(0.875, result.getValue(), 0.125);
	}

	@Tag("integration")
	@Test
	void testEvaluateRelevanceWithPartialSimilarity()
	{
		Sample sample = Sample.builder()
			.withQuestion(QUESTION)
			.withAnswer("Paris is located in France.")
			.build();

		Evaluation result = evaluator.evaluate(sample);

		assertEquals(1.0, result.getValue(), 0.01);
		assertEquals("Answer relevance", result.getName());

		// Expect a value less than 1.0 due to partial similarity
		assertTrue(result.getValue() < 1.0);
	}

	@Tag("integration")
	@Test
	void testEvaluateRelevanceWithEmptyAnswer()
	{
		Sample sample = Sample.builder()
			.withQuestion(QUESTION)
			.withAnswer("")
			.build();

		Evaluation result = evaluator.evaluate(sample);

		assertEquals("Answer relevance", result.getName());
		assertEquals(0.05, result.getValue(), 0.05);
	}

	@Tag("integration")
	@Test
	void testEvaluateRelevanceWithNullAnswer()
	{
		Sample sample = Sample.builder()
			.withQuestion(QUESTION)
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
	void testEvaluateRelevanceWithNullQuestion()
	{
		Sample sample = Sample.builder()
			.withAnswer(ANSWER)
			.withQuestion(null)
			.build();

		try
		{
			evaluator.evaluate(sample);
		}
		catch (IllegalStateException e)
		{
			assertEquals("Attribute not found: question", e.getMessage());
		}
	}

	@Tag("integration")
	@Test
	void shouldEvaluateHigh()
	{
		Sample sample = Sample.builder()
			.withQuestion(QUESTION)
			.withAnswer(ANSWER)
			.withGroundTruth(GROUND_TRUTH)
			.withContextsList(new ArrayList<>())
			.build();

		assertEquals(0.85, evaluator.evaluate(sample).getValue(), 0.15);
	}
}