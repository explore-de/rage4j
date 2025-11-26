package dev.rage4j.model;

import dev.rage4j.LoggingTestWatcher;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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
	void testGetQuestion()
	{
		assertEquals(QUESTION, sample.getQuestion());
	}

	@Test
	void testGetQuestionReturnsNullWhenNotSet()
	{
		Sample nullQuestionSample = Sample.builder()
			.withAnswer(ANSWER)
			.withGroundTruth(GROUND_TRUTH)
			.withContextsList(CONTEXTS)
			.build();

		assertNull(nullQuestionSample.getQuestion());
	}

	@Test
	void testEqualsAndHashCode()
	{
		EqualsVerifier.forClass(Sample.class)
			.usingGetClass()
			.suppress(Warning.NONFINAL_FIELDS)
			.verify();
	}

	@Test
	void testGetAnswer()
	{
		assertEquals(ANSWER, sample.getAnswer());
	}

	@Test
	void testGetAnswerReturnsNullWhenNotSet()
	{
		Sample nullAnswerSample = Sample.builder()
			.withQuestion(QUESTION)
			.withGroundTruth(GROUND_TRUTH)
			.withContextsList(CONTEXTS)
			.build();

		assertNull(nullAnswerSample.getAnswer());
	}

	@Test
	void testGetGroundTruth()
	{
		assertEquals(GROUND_TRUTH, sample.getGroundTruth());
	}

	@Test
	void testGetGroundTruthReturnsNullWhenNotSet()
	{
		Sample nullGroundTruthSample = Sample.builder()
			.withQuestion(QUESTION)
			.withAnswer(ANSWER)
			.withContextsList(CONTEXTS)
			.build();

		assertNull(nullGroundTruthSample.getGroundTruth());
	}

	@Test
	void testGetContextsList()
	{
		assertEquals(CONTEXTS, sample.getContextsList());
	}

	@Test
	void testGetContextsListReturnsNullWhenNotSet()
	{
		Sample nullContextsSample = Sample.builder()
			.withQuestion(QUESTION)
			.withAnswer(ANSWER)
			.withGroundTruth(GROUND_TRUTH)
			.build();

		assertNull(nullContextsSample.getContextsList());
	}
}
