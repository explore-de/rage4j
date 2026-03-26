package dev.rage4j.evaluation.answerrelevance.llm;

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
class AnswerRelevanceLlmEvaluatorIntegrationTest
{
	private static final String QUESTION = "What is the capital of France?";
	private static final String ANSWER_IRRELEVANT = "The sun is a star at the center of our solar system.";
	private static final String ANSWER_TARGET_SCORE_ONE = "France has a capital.";
	private static final String ANSWER_TARGET_SCORE_TWO = "Paris is a major city in France";
	private static final String ANSWER_TARGET_SCORE_THREE = "The capital of France is Paris.";

	private static final String OPEN_AI_KEY = ConfigFactory.getConfig().OPEN_AI_KEY();
	private static final String OPEN_AI_MODEL = ConfigFactory.getConfig().OPEN_AI_MODEL();

	private AnswerRelevanceLlmEvaluator evaluator;

	@BeforeEach
	void setUp()
	{
		// Initialize the evaluator using OpenAIChatModel
		OpenAiChatModel model = OpenAiChatModel.builder()
			.apiKey(OPEN_AI_KEY)
			.modelName(OPEN_AI_MODEL)
			.build();

		evaluator = new AnswerRelevanceLlmEvaluator(model);
	}

	@Tag("integration")
	@Test
	void testEvaluateRelevanceLlmWithRelevantAnswer()
	{
		Sample sample = Sample.builder()
			.withQuestion(QUESTION)
			.withAnswer(ANSWER_TARGET_SCORE_THREE)
			.build();

		Evaluation result = evaluator.evaluate(sample);

		assertEquals("Answer relevance llm", result.getName());
		assertEquals(1.0, result.getValue(),0.01, "Expected normalized score = 1.0 for a relevant answer");
	}

	@Tag("integration")
	@Test
	void testEvaluateRelevanceLlmWithIrrelevantAnswer()
	{
		Sample sample = Sample.builder()
			.withQuestion(QUESTION)
			.withAnswer(ANSWER_IRRELEVANT)
			.build();

		Evaluation result = evaluator.evaluate(sample);

		assertEquals("Answer relevance llm", result.getName());
		assertTrue(result.getValue() <= 1, "Expected score <= 1 for an irrelevant answer");
	}

	@Tag("integration")
	@Test
	void testEvaluateRelevanceLlmTriesToReachScoreOne()
	{
		Sample sample = Sample.builder()
			.withQuestion(QUESTION)
			.withAnswer(ANSWER_TARGET_SCORE_ONE)
			.build();

		Evaluation result = evaluator.evaluate(sample);

		assertEquals("Answer relevance llm", result.getName());
		assertEquals(1.0 / 3.0, result.getValue(),0.1, "Expected normalized score 1/3 for a partially relevant answer");
	}

	@Tag("integration")
	@Test
	void testEvaluateRelevanceLlmTriesToReachScoreTwo()
	{
		Sample sample = Sample.builder()
			.withQuestion(QUESTION)
			.withAnswer(ANSWER_TARGET_SCORE_TWO)
			.build();

		Evaluation result = evaluator.evaluate(sample);

		assertEquals("Answer relevance llm", result.getName());
		assertEquals(2.0 / 3.0, result.getValue(), 0.1, "Expected normalized score 2/3 for a mostly relevant answer");
	}

	@Tag("integration")
	@Test
	void testEvaluateRelevanceLlmNullAnswer()
	{
		Sample sample = Sample.builder()
			.withQuestion(QUESTION)
			.withAnswer(null)
			.build();

		IllegalArgumentException exception = assertThrows(
			IllegalArgumentException.class,
			() -> evaluator.evaluate(sample));
		assertEquals("Sample must have an answer for Answer Relevance LLM evaluation", exception.getMessage());
	}

	@Tag("integration")
	@Test
	void testEvaluateRelevanceLlmNullQuestion()
	{
		Sample sample = Sample.builder()
			.withAnswer(ANSWER_TARGET_SCORE_THREE)
			.withQuestion(null)
			.build();

		IllegalArgumentException exception = assertThrows(
			IllegalArgumentException.class,
			() -> evaluator.evaluate(sample));
		assertEquals("Sample must have a question for Answer Relevance LLM evaluation", exception.getMessage());
	}

	@Tag("integration")
	@Test
	void testEverythingLlmCorrect()
	{
		Sample sample = Sample.builder()
			.withQuestion(QUESTION)
			.withAnswer(ANSWER_TARGET_SCORE_THREE)
			.build();

		Evaluation result = evaluator.evaluate(sample);

		assertEquals("Answer relevance llm", result.getName());
		assertTrue(result.getValue() >= 0 && result.getValue() <= 1, "Score must be in range [0, 1]");
	}
}
