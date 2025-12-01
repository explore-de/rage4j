package dev.rage4j.persist.junit5;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import dev.rage4j.model.EvaluationAggregation;
import dev.rage4j.model.Sample;
import dev.rage4j.persist.EvaluationStore;
import dev.rage4j.persist.store.CompositeStore;
import dev.rage4j.persist.store.JsonLinesStore;

class Rage4jPersistExtensionTest
{
	@TempDir
	static Path tempDir;

	@Test
	void extensionConfigAnnotationWorks()
	{
		@Rage4jPersistConfig(file = "target/test-evaluations.jsonl")
		class TestClass
		{
		}

		Rage4jPersistConfig config = TestClass.class.getAnnotation(Rage4jPersistConfig.class);
		assertNotNull(config);
		assertEquals("target/test-evaluations.jsonl", config.file());
		assertEquals(JsonLinesStore.class, config.storeClass());
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
		assertEquals(JsonLinesStore.class, config.storeClass());
	}

	@Test
	void customStoreClassConfigIsApplied()
	{
		@Rage4jPersistConfig(file = "custom/evaluations.jsonl", storeClass = JsonLinesStore.class)
		class CustomConfig
		{
		}

		Rage4jPersistConfig config = CustomConfig.class.getAnnotation(Rage4jPersistConfig.class);
		assertNotNull(config);
		assertEquals("custom/evaluations.jsonl", config.file());
		assertEquals(JsonLinesStore.class, config.storeClass());
	}

	@Test
	void jsonLinesStoreWorks() throws IOException
	{
		Path jsonlFile = tempDir.resolve("test.jsonl");

		EvaluationStore store = new JsonLinesStore(jsonlFile);
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

	@Test
	void compositeStoreWorks() throws IOException
	{
		Path jsonlFile1 = tempDir.resolve("composite1.jsonl");
		Path jsonlFile2 = tempDir.resolve("composite2.jsonl");

		EvaluationStore store = new CompositeStore(new JsonLinesStore(jsonlFile1), new JsonLinesStore(jsonlFile2));
		assertNotNull(store);

		Sample sample = Sample.builder().withQuestion("Test question").withAnswer("Test answer").build();

		EvaluationAggregation aggregation = new EvaluationAggregation(sample);
		aggregation.put("score", 0.95);

		store.store(aggregation);
		store.close();

		assertTrue(Files.exists(jsonlFile1));
		assertTrue(Files.exists(jsonlFile2));

		String content1 = Files.readString(jsonlFile1);
		String content2 = Files.readString(jsonlFile2);
		assertTrue(content1.contains("Test question"));
		assertTrue(content2.contains("Test question"));
	}
}
