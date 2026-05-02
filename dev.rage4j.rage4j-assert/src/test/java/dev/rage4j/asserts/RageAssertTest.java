package dev.rage4j.asserts;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.rage4j.asserts.exception.Rage4JBleuScoreException;
import dev.rage4j.asserts.exception.Rage4JCorrectnessException;
import dev.rage4j.asserts.exception.Rage4JFaithfulnessException;
import dev.rage4j.asserts.exception.Rage4JRelevanceException;
import dev.rage4j.asserts.exception.Rage4JRougeScoreException;
import dev.rage4j.asserts.exception.Rage4JSimilarityException;
import dev.rage4j.asserts.openai.OpenAiLLMBuilder;
import dev.rage4j.evaluation.Evaluation;
import dev.rage4j.evaluation.rougescore.RougeScoreEvaluator;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O_MINI;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

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

	@Test
	void testCorrectnessApi()
	{
		RageAssert rageAssert = openAiRageAssert();
		rageAssert.given()
			.question(QUESTION)
			.groundTruth(GROUND_TRUTH)
			.when()
			.answer(openAiModel().chat(QUESTION))
			.then()
			.assertAnswerCorrectness(0.7);
	}

	@Test
	void shouldThrowCorrectnessException()
	{
		RageAssert rageAssert = openAiRageAssert();
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
		RageAssert rageAssert = openAiRageAssert();
		rageAssert.given()
			.question(QUESTION)
			.groundTruth(GROUND_TRUTH)
			.contextList(List.of(ANSWER))
			.when()
			.answer(openAiModel()::chat)
			.then()
			.assertFaithfulness(0.7);
	}

	@Test
	void shouldThrowFaithfulnessException()
	{
		RageAssert rageAssert = openAiRageAssert();
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
		RageAssert rageAssert = openAiRageAssert();
		rageAssert.given()
			.question(QUESTION)
			.groundTruth(GROUND_TRUTH)
			.when()
			.answer(openAiModel()::chat)
			.then()
			.assertSemanticSimilarity(0.7);
	}

	@Test
	void shouldThrowSemanticSimilarityException()
	{
		RageAssert rageAssert = openAiRageAssert();
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
		RageAssert rageAssert = openAiRageAssert();
		rageAssert.given()
			.question(QUESTION)
			.groundTruth(GROUND_TRUTH)
			.contextList(CONTEXT)
			.when()
			.answer(openAiModel()::chat)
			.then()
			.assertAnswerRelevance(0.7);
	}

	@Test
	void shouldThrowRelevanceException()
	{
		RageAssert rageAssert = openAiRageAssert();
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

	@Test
	void testBleuScoreApi()
	{
		RageAssert rageAssert = openAiRageAssert();
		rageAssert.given()
			.question(QUESTION)
			.groundTruth(GROUND_TRUTH)
			.when()
			.answer(openAiModel()::chat)
			.then()
			.assertBleuScore(0.7);
	}

	@Test
	void shouldThrowBleuScoreException()
	{
		RageAssert rageAssert = openAiRageAssert();
		RageAssertTestCaseAssertions testCaseAssertions = rageAssert.given()
			.question(QUESTION)
			.groundTruth(GROUND_TRUTH)
			.when()
			.answer(a -> ANSWER_WRONG)
			.then();

		Rage4JBleuScoreException ex = assertThrows(
			Rage4JBleuScoreException.class,
			() -> testCaseAssertions.assertBleuScore(1.1));

		assertTrue(ex.getMessage().startsWith(MINVALUE));
	}

	@Test
	void shouldEvaluateBleuScoreWithoutThreshold()
	{
		AssertionEvaluation assertionEvaluation = new RageAssert((ChatModel) null).given()
			.groundTruth(GROUND_TRUTH)
			.when()
			.answer(ANSWER)
			.then()
			.assertBleuScore();

		Evaluation evaluation = assertionEvaluation.getEvaluation();
		assertEquals("BLEU score", evaluation.getName());
		assertTrue(evaluation.getValue() >= 0.0);
	}

	@Test
	void testRougeScoreApi()
	{
		RageAssert rageAssert = openAiRageAssert();
		rageAssert.given()
			.question(QUESTION)
			.groundTruth(GROUND_TRUTH)
			.when()
			.answer(openAiModel()::chat)
			.then()
			.assertRougeScore(0.7, RougeScoreEvaluator.RougeType.ROUGE1, RougeScoreEvaluator.MeasureType.F1SCORE);
	}

	@Test
	void shouldThrowRougeScoreException()
	{
		RageAssert rageAssert = openAiRageAssert();
		RageAssertTestCaseAssertions testCaseAssertions = rageAssert.given()
			.question(QUESTION)
			.groundTruth(GROUND_TRUTH)
			.when()
			.answer(a -> ANSWER_WRONG)
			.then();

		Rage4JRougeScoreException ex = assertThrows(
			Rage4JRougeScoreException.class,
			() -> testCaseAssertions.assertRougeScore(0.9, RougeScoreEvaluator.RougeType.ROUGE1, RougeScoreEvaluator.MeasureType.PRECISION));

		assertTrue(ex.getMessage().startsWith(MINVALUE));
	}

	@Test
	void shouldEvaluateRougeScoreWithoutThreshold()
	{
		AssertionEvaluation assertionEvaluation = new RageAssert((ChatModel) null).given()
			.groundTruth(GROUND_TRUTH)
			.when()
			.answer(ANSWER)
			.then()
			.assertRougeScore();

		Evaluation evaluation = assertionEvaluation.getEvaluation();
		assertEquals("ROUGE score ROUGE1", evaluation.getName());
		assertTrue(evaluation.getValue() >= 0.0);
	}

	private RageAssert openAiRageAssert()
	{
		return new OpenAiLLMBuilder().fromApiKey(obtainOpenAiKey());
	}

	private OpenAiChatModel openAiModel()
	{
		return OpenAiChatModel.builder()
			.apiKey(obtainOpenAiKey())
			.modelName(GPT_4_O_MINI)
			.build();
	}

	private static String obtainOpenAiKey()
	{
		Optional<String> openAiKey = ConfigProvider.getConfig().getOptionalValue("open.ai.key", String.class);
		assumeTrue(openAiKey.isPresent(), "Set config property open.ai.key to run OpenAI-backed RageAssert tests.");
		return openAiKey.get();
	}
}
