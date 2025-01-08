package dev.rage4j.model;

import dev.rage4j.LoggingTestWatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(LoggingTestWatcher.class)
class SampleTest
{
	private static final String QUESTION = "What is the capital of France?";
	private static final String ANSWER = "Paris is the capital of France.";
	private static final String GROUND_TRUTH = "Paris";
	private static final List<String> CONTEXTS = List.of("Paris is the capital of France.");

	private Sample sample;

	@BeforeEach
	void setUp()
	{
		sample = Sample.builder()
			.withQuestion(QUESTION)
			.withAnswer(ANSWER)
			.withGroundTruth(GROUND_TRUTH)
			.withContextsList(CONTEXTS)
			.build();
	}

	@Test
	void testGetQuestionOrFail()
	{
		assertEquals(QUESTION, sample.getQuestionOrFail());
	}

	@Test
	void testGetQuestionOrFailNullQuestion()
	{
		Sample nullQuestionSample = Sample.builder()
			.withAnswer(ANSWER)
			.withGroundTruth(GROUND_TRUTH)
			.withContextsList(CONTEXTS)
			.build();

		assertThrows(IllegalStateException.class, nullQuestionSample::getQuestionOrFail);
	}

	@Test
	void testGetAnswerOrFail()
	{
		assertEquals(ANSWER, sample.getAnswerOrFail());
	}

	@Test
	void testGetAnswerOrFailNullAnswer()
	{
		Sample nullAnswerSample = Sample.builder()
			.withQuestion(QUESTION)
			.withGroundTruth(GROUND_TRUTH)
			.withContextsList(CONTEXTS)
			.build();

		assertThrows(IllegalStateException.class, nullAnswerSample::getAnswerOrFail);
	}

	@Test
	void testGetGroundTruthOrFail()
	{
		assertEquals(GROUND_TRUTH, sample.getGroundTruthOrFail());
	}

	@Test
	void testGetGroundTruthOrFailNullGroundTruth()
	{
		Sample nullGroundTruthSample = Sample.builder()
			.withQuestion(QUESTION)
			.withAnswer(ANSWER)
			.withContextsList(CONTEXTS)
			.build();

		assertThrows(IllegalStateException.class, nullGroundTruthSample::getGroundTruthOrFail);
	}

	@Test
	void testGetContextsListOrFail()
	{
		assertEquals(CONTEXTS, sample.getContextsListOrFail());
	}

	@Test
	void testGetContextsListOrFailNullContextsList()
	{
		Sample nullContextsSample = Sample.builder()
			.withQuestion(QUESTION)
			.withAnswer(ANSWER)
			.withGroundTruth(GROUND_TRUTH)
			.build();

		assertThrows(IllegalStateException.class, nullContextsSample::getContextsListOrFail);
	}
}
