package dev.rage4j.evaluation.axcel;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class AxcelDataLoaderTest
{
	static AxcelOneShotExamples examples;

	@BeforeAll
	static void setup()
	{
		examples = new AxcelDataLoader().loadExampleData();
	}

	@Test
	void examplesNotNull()
	{
		assertNotNull(examples);
	}

	@Test
	void sourceTextNotNull()
	{
		assertNotNull(examples.sourceText());
	}

	@Test
	void derivedTextNotNull()
	{
		assertNotNull(examples.derivedText());
	}

	@Test
	void aiResponseNotNull()
	{
		assertNotNull(examples.aiResponse());
	}
}