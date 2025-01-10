package dev.rage4j.asserts;

import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.rage4j.asserts.exception.Rage4JCorrectnessException;
import dev.rage4j.asserts.exception.Rage4JFaithfulnessException;
import dev.rage4j.asserts.exception.Rage4JRelevanceException;
import dev.rage4j.asserts.exception.Rage4JSimilarityException;
import dev.rage4j.asserts.openai.OpenAiLLMBuilder;
import org.junit.jupiter.api.Test;

import java.util.List;

import static dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O_MINI;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RageAssertTest
{
	private static final String MINVALUE = "Answer did not reach required min value!";
	private static final String QUESTION = "What is the capital of France?";
	private static final String GROUND_TRUTH = "The capital of France is Paris.";
	private static final String ANSWER = "The capital of France is Paris.";
	private static final String ANSWER_WRONG = "The capital of France is Berlin.";
	private static final String ANSWER_WRONG_RELEVANCE = "The weather in Paris is nice today.";
	private static final List<String> CONTEXT = List.of(
		"Paris is the capital and largest city of France.");
	private final String key = System.getenv("OPEN_API_KEY");
	private final OpenAiChatModel model = OpenAiChatModel.builder()
		.apiKey(key)
		.modelName(GPT_4_O_MINI)
		.build();

	@Test
	void testCorrectnessApi()
	{
		RageAssert rageAssert = new OpenAiLLMBuilder().fromApiKey(key);
		rageAssert.given()
			.question(QUESTION)
			.groundTruth(GROUND_TRUTH)
			.when()
			.answer(model.generate(QUESTION))
			.then()
			.assertAnswerCorrectness(0.7);
	}

	@Test
	void shouldThrowCorrectnessException()
	{
		RageAssert rageAssert = new OpenAiLLMBuilder().fromApiKey(key);
		RageAssertTestCaseAssertions testCaseAssertions = rageAssert.given()
			.question(QUESTION)
			.groundTruth(GROUND_TRUTH)
			.when()
			.answer(a -> ANSWER_WRONG)
			.then();

		Rage4JCorrectnessException ex = assertThrows(
			Rage4JCorrectnessException.class,
			() -> testCaseAssertions.assertAnswerCorrectness(0.7));

		assertTrue(ex.getMessage().startsWith(MINVALUE));
	}

	@Test
	void testFaithfulApi()
	{
		RageAssert rageAssert = new OpenAiLLMBuilder().fromApiKey(key);
		rageAssert.given()
			.question(QUESTION)
			.groundTruth(GROUND_TRUTH)
			.contextList(List.of(ANSWER))
			.when()
			.answer(model::generate)
			.then()
			.assertFaithfulness(0.7);
	}

	@Test
	void shouldThrowFaithfulnessException()
	{
		RageAssert rageAssert = new OpenAiLLMBuilder().fromApiKey(key);
		RageAssertTestCaseAssertions testCaseAssertions = rageAssert.given()
			.question(QUESTION)
			.groundTruth(GROUND_TRUTH)
			.contextList(List.of(ANSWER))
			.when()
			.answer(a -> ANSWER_WRONG)
			.then();

		Rage4JFaithfulnessException ex = assertThrows(
			Rage4JFaithfulnessException.class,
			() -> testCaseAssertions.assertFaithfulness(1.1));

		assertTrue(ex.getMessage().startsWith(MINVALUE));
	}

	@Test
	void testSemanticSimilarityApi()
	{
		RageAssert rageAssert = new OpenAiLLMBuilder().fromApiKey(key);
		rageAssert.given()
			.question(QUESTION)
			.groundTruth(GROUND_TRUTH)
			.when()
			.answer(model::generate)
			.then()
			.assertSemanticSimilarity(0.7);
	}

	@Test
	void shouldThrowSemanticSimilarityException()
	{
		RageAssert rageAssert = new OpenAiLLMBuilder().fromApiKey(key);
		RageAssertTestCaseAssertions testCaseAssertions = rageAssert.given()
			.question(QUESTION)
			.groundTruth(GROUND_TRUTH)
			.when()
			.answer(a -> ANSWER_WRONG)
			.then();

		Rage4JSimilarityException ex = assertThrows(
			Rage4JSimilarityException.class,
			() -> testCaseAssertions.assertSemanticSimilarity(1.1));

		assertTrue(ex.getMessage().startsWith(MINVALUE));
	}

	@Test
	void testAnswerRelevanceApi()
	{
		RageAssert rageAssert = new OpenAiLLMBuilder().fromApiKey(key);
		rageAssert.given()
			.question(QUESTION)
			.groundTruth(GROUND_TRUTH)
			.contextList(CONTEXT)
			.when()
			.answer(model::generate)
			.then()
			.assertAnswerRelevance(0.7);
	}

	@Test
	void shouldThrowRelevanceException()
	{
		RageAssert rageAssert = new OpenAiLLMBuilder().fromApiKey(key);
		RageAssertTestCaseAssertions testCaseAssertions = rageAssert.given()
			.question(QUESTION)
			.groundTruth(GROUND_TRUTH)
			.contextList(CONTEXT)
			.when()
			.answer(a -> ANSWER_WRONG_RELEVANCE)
			.then();

		Rage4JRelevanceException ex = assertThrows(
			Rage4JRelevanceException.class,
			() -> testCaseAssertions.assertAnswerRelevance(1.1));

		assertTrue(ex.getMessage().startsWith(MINVALUE));
	}
}