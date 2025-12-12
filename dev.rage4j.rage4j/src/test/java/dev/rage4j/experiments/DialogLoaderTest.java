package dev.rage4j.experiments;

import dev.rage4j.LoggingTestWatcher;
import dev.rage4j.model.Sample;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(LoggingTestWatcher.class)
class DialogLoaderTest
{
	private DialogLoader dialogLoader;

	@BeforeEach
	void setUp()
	{
		dialogLoader = new DialogLoader();
	}

	@Test
	void testGetDialog_shouldReturnSample()
	{
		Sample sample = dialogLoader.getDialog();

		assertNotNull(sample, "Sample should not be null");
		assertTrue(sample.hasQuestion(), "Sample should have a question");
		assertTrue(sample.hasAnswer(), "Sample should have an answer");
		assertTrue(sample.hasContext(), "Sample should have contexts");
		assertNotNull(sample.getQuestion(), "Question should not be null");
		assertNotNull(sample.getAnswer(), "Answer should not be null");
		assertNotNull(sample.getContext(), "Contexts should not be null");
	}

	@Test
	void testGetDialog_shouldHaveCorrectFormat()
	{
		Sample sample = dialogLoader.getDialog();

		assertTrue(sample.getQuestion().contains(":"), "Question should be in format 'role: message'");
		assertTrue(sample.getAnswer().contains(":"), "Answer should be in format 'role: message'");
		assertTrue(sample.getContext().contains(":"), "Context should contain at least one message in format 'role: message'");
	}

	@Test
	void testGetDialog_shouldCycleThroughMultipleDialogs()
	{
		Sample firstSample = dialogLoader.getDialog();
		assertNotNull(firstSample, "First sample should not be null");

		// Call getDialog multiple times to test cycling (10 times to reproduce the bug)
		for (int i = 0; i < 10; i++)
		{
			Sample sample = dialogLoader.getDialog();
			assertNotNull(sample, "Sample at iteration " + i + " should not be null");
			assertTrue(sample.hasQuestion(), "Sample at iteration " + i + " should have a question");
			assertTrue(sample.hasAnswer(), "Sample at iteration " + i + " should have an answer");
		}
	}

	@Test
	void testGetDialog_shouldNotThrowIndexOutOfBounds()
	{
		// This test specifically reproduces the "Index 10 out of bounds for length 10" error
		assertDoesNotThrow(() -> {
			for (int i = 0; i < 20; i++)
			{
				dialogLoader.getDialog();
			}
		}, "Should not throw IndexOutOfBoundsException when cycling through dialogs");
	}

	@Test
	void testGetDialog_contextShouldNotIncludeQuestionOrAnswer()
	{
		Sample sample = dialogLoader.getDialog();

		// The context should not contain the question or answer (last two messages)
		assertTrue(sample.hasContext(), "Sample should have contexts");
		assertFalse(sample.getContext().contains(sample.getQuestion()),
			"Context should not include the question");
		assertFalse(sample.getContext().contains(sample.getAnswer()),
			"Context should not include the answer");
	}
}
