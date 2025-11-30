package dev.rage4j.persist;

import java.io.Closeable;

import dev.rage4j.model.EvaluationAggregation;

/**
 * Interface for storing evaluation results. Implementations can persist
 * evaluations to various backends such as JSON Lines files or databases.
 *
 * <p>
 * Example usage:
 * </p>
 *
 * <pre>
 * try (EvaluationStore store = new JsonLinesStore(Path.of("target/evaluations.jsonl"))) {
 *     EvaluationAggregation result = EvaluationAggregator.evaluateAll(sample, evaluators);
 *     store.store(result);
 * } // flush() is called automatically on close()
 * </pre>
 */
public interface EvaluationStore extends Closeable
{

	/**
	 * Stores an evaluation aggregation to the buffer. The data is not persisted
	 * until {@link #flush()} is called.
	 *
	 * @param aggregation
	 *            The evaluation aggregation to store. Must contain a Sample.
	 */
	void store(EvaluationAggregation aggregation);

	/**
	 * Flushes any buffered data to the underlying storage. This should be
	 * called to ensure all data is persisted before reading the output files.
	 */
	void flush();

	/**
	 * Stores an evaluation aggregation and immediately flushes to the
	 * underlying storage. This is a convenience method that combines
	 * {@link #store(EvaluationAggregation)} and {@link #flush()} into a single
	 * call.
	 *
	 * @param aggregation
	 *            The evaluation aggregation to store.
	 */
	default void storeFlush(EvaluationAggregation aggregation)
	{
		store(aggregation);
		flush();
	}

	/**
	 * Closes the store and releases any resources. This also flushes any
	 * remaining buffered data.
	 */
	@Override
	void close();
}
