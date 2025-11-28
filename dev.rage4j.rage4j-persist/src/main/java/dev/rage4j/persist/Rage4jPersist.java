package dev.rage4j.persist;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;

import dev.rage4j.model.EvaluationAggregation;
import dev.rage4j.persist.store.CompositeStore;
import dev.rage4j.persist.store.JsonLinesStore;

/**
 * Main entry point for the rage4j-persist module. Provides factory methods for
 * creating evaluation stores and a static API for recording evaluations.
 *
 * <h2>Usage Examples</h2>
 *
 * <h3>Creating stores:</h3>
 *
 * <pre>
 * // JSONL store
 * EvaluationStore store = Rage4jPersist.jsonLines("target/results.jsonl");
 *
 * // Composite store (write to multiple destinations)
 * EvaluationStore store = Rage4jPersist.composite(
 *     Rage4jPersist.jsonLines("target/results.jsonl"),
 *     Rage4jPersist.jsonLines("target/backup.jsonl")
 * );
 * </pre>
 *
 * <h3>Static API:</h3>
 *
 * <pre>
 * // Configure once
 * Rage4jPersist.configure(Rage4jPersist.jsonLines("target/evaluations.jsonl"));
 *
 * // Record anywhere
 * EvaluationAggregation result = EvaluationAggregator.evaluateAll(sample, evaluators);
 * Rage4jPersist.record(result);
 * </pre>
 */
public final class Rage4jPersist
{

	private static final AtomicReference<EvaluationStore> CONFIGURED_STORE = new AtomicReference<>();
	private static final ThreadLocal<RecordMetadata> TEST_CONTEXT = new ThreadLocal<>();

	private Rage4jPersist()
	{
		throw new IllegalStateException("Rage4jPersist is a utility class and should not be instantiated.");
	}

	// ========================================================================
	// Factory methods
	// ========================================================================

	/**
	 * Creates a JSON Lines store that writes to the specified file.
	 *
	 * @param file
	 *            The file path (as a String).
	 * @return A new JsonLinesStore instance.
	 */
	public static EvaluationStore jsonLines(String file)
	{
		return jsonLines(Path.of(file));
	}

	/**
	 * Creates a JSON Lines store that writes to the specified file.
	 *
	 * @param file
	 *            The file path.
	 * @return A new JsonLinesStore instance.
	 */
	public static EvaluationStore jsonLines(Path file)
	{
		return new JsonLinesStore(file);
	}

	/**
	 * Creates a composite store that delegates to multiple stores.
	 *
	 * @param stores
	 *            The stores to delegate to.
	 * @return A new CompositeStore instance.
	 */
	public static EvaluationStore composite(EvaluationStore... stores)
	{
		return new CompositeStore(stores);
	}

	/**
	 * Configures the global store for static API usage.
	 *
	 * @param store
	 *            The store to use for static record() calls.
	 */
	public static void configure(EvaluationStore store)
	{
		CONFIGURED_STORE.set(store);
	}

	/**
	 * Returns the currently configured store.
	 *
	 * @return The configured store, or null if not configured.
	 */
	public static EvaluationStore configured()
	{
		return CONFIGURED_STORE.get();
	}

	/**
	 * Records an evaluation using the configured store.
	 *
	 * @param aggregation
	 *            The evaluation aggregation to record.
	 * @throws IllegalStateException
	 *             if no store has been configured.
	 */
	public static void store(EvaluationAggregation aggregation)
	{
		EvaluationStore store = CONFIGURED_STORE.get();
		if (store == null)
		{
			throw new IllegalStateException(
				"No store configured. Call Rage4jPersist.configure(store) first or use a store instance directly.");
		}
		store.store(aggregation);
	}

	/**
	 * Records an evaluation with metadata using the configured store.
	 *
	 * @param aggregation
	 *            The evaluation aggregation to record.
	 * @param metadata
	 *            The metadata for this record.
	 * @throws IllegalStateException
	 *             if no store has been configured.
	 */
	public static void store(EvaluationAggregation aggregation, RecordMetadata metadata)
	{
		EvaluationStore store = CONFIGURED_STORE.get();
		if (store == null)
		{
			throw new IllegalStateException(
				"No store configured. Call Rage4jPersist.configure(store) first or use a store instance directly.");
		}
		store.store(aggregation, metadata);
	}

	/**
	 * Flushes the configured store.
	 *
	 * @throws IllegalStateException
	 *             if no store has been configured.
	 */
	public static void flush()
	{
		EvaluationStore store = CONFIGURED_STORE.get();
		if (store == null)
		{
			throw new IllegalStateException("No store configured.");
		}
		store.flush();
	}

	/**
	 * Resets the configured store. This is primarily for testing purposes.
	 */
	public static void reset()
	{
		EvaluationStore store = CONFIGURED_STORE.getAndSet(null);
		if (store != null)
		{
			store.close();
		}
		TEST_CONTEXT.remove();
	}

	/**
	 * Clears the configured store without closing it. Used by JUnit extensions
	 * when the store lifecycle is managed externally.
	 */
	public static void clearConfiguration()
	{
		CONFIGURED_STORE.set(null);
		TEST_CONTEXT.remove();
	}

	/**
	 * Sets the current test context metadata. This is typically called by JUnit
	 * extensions before each test method.
	 *
	 * @param metadata
	 *            The test context metadata.
	 */
	public static void setTestContext(RecordMetadata metadata)
	{
		TEST_CONTEXT.set(metadata);
	}

	/**
	 * Gets the current test context metadata.
	 *
	 * @return The current test context, or null if not set.
	 */
	public static RecordMetadata getTestContext()
	{
		return TEST_CONTEXT.get();
	}

	/**
	 * Clears the current test context metadata.
	 */
	public static void clearTestContext()
	{
		TEST_CONTEXT.remove();
	}
}
