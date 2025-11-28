package dev.rage4j.persist;

import java.io.Closeable;

import dev.rage4j.model.EvaluationAggregation;

/**
 * Interface for storing evaluation results. Implementations can persist
 * evaluations to various backends such as CSV files, JSON files, or databases.
 *
 * <p>
 * Example usage:
 * </p>
 *
 * <pre>
 * try (EvaluationStore store = Rage4jPersist.csv("target/evaluations")) {
 *     EvaluationAggregation result = EvaluationAggregator.evaluateAll(sample, evaluators);
 *     store.store(result);
 * }
 * </pre>
 */
public interface EvaluationStore extends Closeable
{

	/**
	 * Stores an evaluation aggregation with default metadata (current
	 * timestamp, no test context).
	 *
	 * @param aggregation
	 *            The evaluation aggregation to store. Must contain a Sample.
	 */
	void store(EvaluationAggregation aggregation);

	/**
	 * Stores an evaluation aggregation with the specified metadata.
	 *
	 * @param aggregation
	 *            The evaluation aggregation to store. Must contain a Sample.
	 * @param metadata
	 *            Metadata about the test context.
	 */
	void store(EvaluationAggregation aggregation, RecordMetadata metadata);

	/**
	 * Flushes any buffered data to the underlying storage. This should be
	 * called to ensure all data is persisted before reading the output files.
	 */
	void flush();

	/**
	 * Closes the store and releases any resources. This also flushes any
	 * remaining buffered data.
	 */
	@Override
	void close();
}
