package dev.rage4j.evaluation.contextrelevance.llm;

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
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(LoggingTestWatcher.class)
class ContextRelevanceLlmEvaluatorIntegrationTest
{
	private static final String QUESTION = "What is the capital of France?";
	private static final String RELEVANT_CONTEXT = "Paris is the capital of France.";
	private static final String IRRELEVANT_CONTEXT = "The Great Wall of China is located in northern China.";

	private static final String OPEN_AI_KEY = ConfigFactory.getConfig().OPEN_AI_KEY();
	private static final String OPEN_AI_MODEL = ConfigFactory.getConfig().OPEN_AI_MODEL();

	private ContextRelevanceLlmEvaluator evaluator;

	@BeforeEach
	void setUp()
	{
		OpenAiChatModel model = OpenAiChatModel.builder()
			.apiKey(OPEN_AI_KEY)
			.modelName(OPEN_AI_MODEL)
			.build();

		evaluator = new ContextRelevanceLlmEvaluator(model);
	}

	@Tag("integration")
	@Test
	void testEvaluateWithHighlyRelevantContext()
	{
		Sample sample = Sample.builder()
			.withQuestion(QUESTION)
			.withContext(RELEVANT_CONTEXT)
			.build();

		Evaluation result = evaluator.evaluate(sample);

		assertEquals("Context relevance LLM", result.getName());
		assertEquals(1.0, result.getValue(), 0.01, "Expected normalized score 1.0 for highly relevant context");
	}

	@Tag("integration")
	@Test
	void testEvaluateWithIrrelevantContext()
	{
		Sample sample = Sample.builder()
			.withQuestion(QUESTION)
			.withContext(IRRELEVANT_CONTEXT)
			.build();

		Evaluation result = evaluator.evaluate(sample);

		assertEquals("Context relevance LLM", result.getName());
		assertEquals(0.0, result.getValue(), 0.01, "Expected score 0.0 for irrelevant context");
	}

	@Tag("integration")
	@Test
	void testEvaluateScoreIsInValidRange()
	{
		Sample sample = Sample.builder()
			.withQuestion(QUESTION)
			.withContext(RELEVANT_CONTEXT)
			.build();

		Evaluation result = evaluator.evaluate(sample);

		assertEquals("Context relevance LLM", result.getName());
		assertTrue(result.getValue() >= 0 && result.getValue() <= 1, "Score must be in range [0, 1]");
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
		assertEquals("Sample must have a context for Context Relevance LLM evaluation", exception.getMessage());
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
		assertEquals("Sample must have a question for Context Relevance LLM evaluation", exception.getMessage());
	}
}