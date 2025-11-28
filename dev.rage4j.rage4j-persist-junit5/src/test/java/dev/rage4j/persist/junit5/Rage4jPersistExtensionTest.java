package dev.rage4j.persist.junit5;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import dev.rage4j.model.EvaluationAggregation;
import dev.rage4j.model.Sample;
import dev.rage4j.persist.EvaluationStore;
import dev.rage4j.persist.Rage4jPersist;

class Rage4jPersistExtensionTest
{

	@TempDir
	static Path tempDir;

	@BeforeAll
	static void setup()
	{
		Rage4jPersist.clearConfiguration();
	}

	@AfterAll
	static void cleanup()
	{
		Rage4jPersist.clearConfiguration();
	}

	@Test
	void extensionInjectsStore()
	{
		@Rage4jPersistConfig(file = "target/test-evaluations.jsonl")
		class TestClass
		{
		}

		Rage4jPersistConfig config = TestClass.class.getAnnotation(Rage4jPersistConfig.class);
		assertNotNull(config);
		assertTrue(config.configureGlobal());
	}

	@Test
	void defaultConfigHasExpectedValues()
	{
		@Rage4jPersistConfig
		class DefaultConfig
		{
		}

		Rage4jPersistConfig config = DefaultConfig.class.getAnnotation(Rage4jPersistConfig.class);
		assertNotNull(config);
		assertEquals("target/evaluations.jsonl", config.file());
		assertTrue(config.configureGlobal());
	}

	@Test
	void customConfigIsApplied()
	{
		@Rage4jPersistConfig(file = "custom/evaluations.jsonl", configureGlobal = false)
		class CustomConfig
		{
		}

		Rage4jPersistConfig config = CustomConfig.class.getAnnotation(Rage4jPersistConfig.class);
		assertNotNull(config);
		assertEquals("custom/evaluations.jsonl", config.file());
		assertFalse(config.configureGlobal());
	}

	@Test
	void testContextIsSet()
	{
		var metadata = dev.rage4j.persist.RecordMetadata.of("TestClass", "testMethod");
		Rage4jPersist.setTestContext(metadata);

		var retrieved = Rage4jPersist.getTestContext();
		assertNotNull(retrieved);
		assertEquals("TestClass", retrieved.testClass());
		assertEquals("testMethod", retrieved.testMethod());
		assertNotNull(retrieved.timestamp());

		Rage4jPersist.clearTestContext();
		assertNull(Rage4jPersist.getTestContext());
	}

	@Test
	void jsonLinesStoreWorks() throws IOException
	{
		Path jsonlFile = tempDir.resolve("test.jsonl");

		EvaluationStore store = Rage4jPersist.jsonLines(jsonlFile);
		assertNotNull(store);

		Sample sample = Sample.builder().withQuestion("What is ML?").withAnswer("ML is machine learning")
			.withGroundTruth("ML is machine learning").build();

		EvaluationAggregation aggregation = new EvaluationAggregation(sample);
		aggregation.put("accuracy", 0.88);

		store.store(aggregation);
		store.close();

		assertTrue(Files.exists(jsonlFile));
		String content = Files.readString(jsonlFile);
		assertTrue(content.contains("What is ML?"));
		assertTrue(content.contains("accuracy"));
	}
}
