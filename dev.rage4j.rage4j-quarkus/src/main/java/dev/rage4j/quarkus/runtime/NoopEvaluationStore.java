package dev.rage4j.quarkus.runtime;

import dev.rage4j.model.EvaluationAggregation;
import dev.rage4j.persist.EvaluationStore;

/**
 * An evaluation store that intentionally discards results.
 *
 * <p>
 * The Quarkus extension exposes an {@link EvaluationStore} bean even when
 * persistence is disabled, which keeps test injection simple and avoids file
 * creation by default.
 * </p>
 */
public final class NoopEvaluationStore implements EvaluationStore
{
	@Override
	public void store(EvaluationAggregation aggregation)
	{
		// Intentionally empty.
	}

	@Override
	public void flush()
	{
		// Intentionally empty.
	}

	@Override
	public void close()
	{
		// Intentionally empty.
	}
}
