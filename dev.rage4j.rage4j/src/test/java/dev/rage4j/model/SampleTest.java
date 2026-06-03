package dev.rage4j.model;

import dev.rage4j.LoggingTestWatcher;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(LoggingTestWatcher.class)
class SampleTest
{
	private static final String QUESTION = "What is the capital of France?";
	private static final String ANSWER = "Paris is the capital of France.";
	private static final String GROUND_TRUTH = "Paris";
	private static final String CONTEXT = "Paris is the capital of France.";

	private Sample sample;

	@BeforeEach
	void setUp()
	{
		sample = Sample.builder()
			.withQuestion(QUESTION)
			.withAnswer(ANSWER)
			.withGroundTruth(GROUND_TRUTH)
			.withContext(CONTEXT)
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
			.withContext(CONTEXT)
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
			.withContext(CONTEXT)
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
			.withContext(CONTEXT)
			.build();

		assertNull(nullGroundTruthSample.getGroundTruth());
	}

	@Test
	void testGetContext()
	{
		assertEquals(CONTEXT, sample.getContext());
	}

	@Test
	void testGetContextReturnsNullWhenNotSet()
	{
		Sample nullContextSample = Sample.builder()
			.withQuestion(QUESTION)
			.withAnswer(ANSWER)
			.withGroundTruth(GROUND_TRUTH)
			.build();

		assertNull(nullContextSample.getContext());
	}

	@Test
	void testGetImagesReturnsEmptyListWhenNotSet()
	{
		assertTrue(sample.getImages().isEmpty());
		assertFalse(sample.hasImages());
	}

	@Test
	void testWithImageAddsSingleImage()
	{
		Rage4jImage image = Rage4jImage.fromBytes(new byte[] { 1, 2, 3 }, "image/png", "eiffel-tower.png");
		Sample withImage = Sample.builder()
			.withQuestion(QUESTION)
			.withContext(CONTEXT)
			.withAnswer(ANSWER)
			.withImage(image)
			.build();

		assertTrue(withImage.hasImages());
		assertEquals(1, withImage.getImages().size());
		assertEquals(image, withImage.getImages().get(0));
	}

	@Test
	void testWithImagesReplacesList()
	{
		Rage4jImage img1 = Rage4jImage.fromBytes(new byte[] { 1 }, "image/png", "a.png");
		Rage4jImage img2 = Rage4jImage.fromBytes(new byte[] { 2 }, "image/png", "b.png");
		Sample withImages = Sample.builder()
			.withQuestion(QUESTION)
			.withImages(List.of(img1, img2))
			.build();

		assertEquals(2, withImages.getImages().size());
	}

	@Test
	void testGetImagesIsUnmodifiable()
	{
		Rage4jImage image = Rage4jImage.fromBytes(new byte[] { 1 }, "image/png", "a.png");
		Sample withImage = Sample.builder()
			.withQuestion(QUESTION)
			.withImage(image)
			.build();

		assertThrows(UnsupportedOperationException.class, () -> withImage.getImages().add(image));
	}
}
