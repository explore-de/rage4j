package dev.rage4j.persist.store;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.rage4j.model.EvaluationAggregation;
import dev.rage4j.model.Sample;

class JsonLinesStoreTest
{
	@TempDir
	Path tempDir;

	private JsonLinesStore store;
	private Path file;
	private ObjectMapper objectMapper;

	@BeforeEach
	void setUp()
	{
		file = tempDir.resolve("evaluations.jsonl");
		store = new JsonLinesStore(file);
		objectMapper = new ObjectMapper();
	}

	@AfterEach
	void tearDown()
	{
		if (store != null)
		{
			store.close();
		}
	}

	@Test
	void testStoreCreatesFile()
	{
		Sample sample = Sample.builder().withQuestion("What is AI?").withAnswer("Artificial Intelligence").build();

		EvaluationAggregation aggregation = new EvaluationAggregation(sample);
		aggregation.put("BLEU score", 0.85);

		store.store(aggregation);
		store.flush();

		assertTrue(Files.exists(file));
	}

	@Test
	void testEachRecordOnSeparateLine() throws IOException
	{
		for (int i = 0; i < 3; i++)
		{
			Sample sample = Sample.builder().withQuestion("Question " + i).withAnswer("Answer " + i).build();

			EvaluationAggregation aggregation = new EvaluationAggregation(sample);
			aggregation.put("score", (double) i / 10);

			store.store(aggregation);
		}
		store.flush();

		List<String> lines = Files.readAllLines(file);
		assertEquals(3, lines.size());
	}

	@Test
	void testRecordContainsValidJson() throws IOException
	{
		Sample sample = Sample.builder().withQuestion("What is AI?").withAnswer("Artificial Intelligence")
			.withGroundTruth("AI is artificial intelligence").build();

		EvaluationAggregation aggregation = new EvaluationAggregation(sample);
		aggregation.put("BLEU score", 0.85);
		aggregation.put("Faithfulness", 0.92);

		store.store(aggregation);
		store.flush();

		List<String> lines = Files.readAllLines(file);
		JsonNode json = objectMapper.readTree(lines.getFirst());

		assertTrue(json.has("sample"));
		assertTrue(json.has("metrics"));
	}

	@Test
	void testSampleFieldsIncluded() throws IOException
	{
		Sample sample = Sample.builder().withQuestion("Test question").withAnswer("Test answer")
			.withGroundTruth("Expected answer").withContext("Some context").build();

		EvaluationAggregation aggregation = new EvaluationAggregation(sample);
		aggregation.put("score", 0.5);

		store.store(aggregation);
		store.flush();

		List<String> lines = Files.readAllLines(file);
		JsonNode json = objectMapper.readTree(lines.getFirst());
		JsonNode sampleNode = json.get("sample");

		assertEquals("Test question", sampleNode.get("question").asText());
		assertEquals("Test answer", sampleNode.get("answer").asText());
		assertEquals("Expected answer", sampleNode.get("groundTruth").asText());
		assertEquals("Some context", sampleNode.get("context").asText());
	}

	@Test
	void testMetricsIncluded() throws IOException
	{
		Sample sample = Sample.builder().withQuestion("Test").withAnswer("Answer").build();

		EvaluationAggregation aggregation = new EvaluationAggregation(sample);
		aggregation.put("BLEU score", 0.85);
		aggregation.put("Faithfulness", 0.92);

		store.store(aggregation);
		store.flush();

		List<String> lines = Files.readAllLines(file);
		JsonNode json = objectMapper.readTree(lines.getFirst());
		JsonNode metricsNode = json.get("metrics");

		assertEquals(0.85, metricsNode.get("BLEU score").asDouble(), 0.001);
		assertEquals(0.92, metricsNode.get("Faithfulness").asDouble(), 0.001);
	}

	@Test
	void testNullFieldsOmitted() throws IOException
	{
		Sample sample = Sample.builder().withQuestion("Test").withAnswer("Answer").build();

		EvaluationAggregation aggregation = new EvaluationAggregation(sample);
		aggregation.put("score", 0.5);

		store.store(aggregation);
		store.flush();

		List<String> lines = Files.readAllLines(file);
		JsonNode json = objectMapper.readTree(lines.getFirst());
		JsonNode sampleNode = json.get("sample");

		assertTrue(sampleNode.has("question"));
		assertTrue(sampleNode.has("answer"));
		// groundTruth and context were not set, should be omitted
		assertTrue(!sampleNode.has("groundTruth") || sampleNode.get("groundTruth").isNull());
	}

	@Test
	void testBufferingDataNotVisibleUntilFlush() throws IOException
	{
		Sample sample = Sample.builder().withQuestion("Test").withAnswer("Answer").build();

		EvaluationAggregation aggregation = new EvaluationAggregation(sample);
		aggregation.put("score", 0.5);

		store.store(aggregation);

		// File should not exist or be empty before flush
		assertTrue(!Files.exists(file) || Files.size(file) == 0);

		store.flush();

		// Now the file should have content
		assertTrue(Files.exists(file));
		assertTrue(Files.size(file) > 0);
	}

	@Test
	void testStoreFlushWritesImmediately() throws IOException
	{
		Sample sample = Sample.builder().withQuestion("Test").withAnswer("Answer").build();

		EvaluationAggregation aggregation = new EvaluationAggregation(sample);
		aggregation.put("score", 0.5);

		store.storeFlush(aggregation);

		// File should have content immediately
		assertTrue(Files.exists(file));
		List<String> lines = Files.readAllLines(file);
		assertEquals(1, lines.size());
	}

	@Test
	void testCloseAutoFlushes() throws IOException
	{
		Sample sample = Sample.builder().withQuestion("Test").withAnswer("Answer").build();

		EvaluationAggregation aggregation = new EvaluationAggregation(sample);
		aggregation.put("score", 0.5);

		store.store(aggregation);
		store.close();
		store = null; // Prevent double close in tearDown

		// File should have content after close
		assertTrue(Files.exists(file));
		List<String> lines = Files.readAllLines(file);
		assertEquals(1, lines.size());
	}
}
