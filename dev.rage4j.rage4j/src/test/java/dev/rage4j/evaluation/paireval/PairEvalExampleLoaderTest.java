package dev.rage4j.evaluation.paireval;

import dev.rage4j.model.Sample;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class PairEvalExampleLoaderTest
{
	private final PairEvalExampleLoader loader = new PairEvalExampleLoader();

	@Test
	void testLoadExampleData()
	{
		List<Sample> examples = loader.loadExampleData();

		assertEquals(3, examples.size());

		Sample first = examples.getFirst();
		assertNotEquals(1, first.getContext().length());
	}
}