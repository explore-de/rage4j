package dev.rage4j.persist.store;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dev.rage4j.model.EvaluationAggregation;
import dev.rage4j.persist.EvaluationStore;
import dev.rage4j.persist.RecordMetadata;

/**
 * An EvaluationStore implementation that delegates to multiple underlying
 * stores. All operations are forwarded to each store in order.
 *
 * <p>
 * This allows writing evaluations to multiple destinations simultaneously, for
 * example both CSV and JSONL files.
 * </p>
 */
public class CompositeStore implements EvaluationStore
{

	private final List<EvaluationStore> stores;
	private boolean closed;

	/**
	 * Creates a new CompositeStore that delegates to the specified stores.
	 *
	 * @param stores
	 *            The underlying stores to delegate to.
	 */
	public CompositeStore(EvaluationStore... stores)
	{
		this.stores = new ArrayList<>(Arrays.asList(stores));
		this.closed = false;
	}

	/**
	 * Creates a new CompositeStore that delegates to the specified stores.
	 *
	 * @param stores
	 *            The underlying stores to delegate to.
	 */
	public CompositeStore(List<EvaluationStore> stores)
	{
		this.stores = new ArrayList<>(stores);
		this.closed = false;
	}

	@Override
	public void store(EvaluationAggregation aggregation)
	{
		checkNotClosed();
		for (EvaluationStore store : stores)
		{
			store.store(aggregation);
		}
	}

	@Override
	public void store(EvaluationAggregation aggregation, RecordMetadata metadata)
	{
		checkNotClosed();
		for (EvaluationStore store : stores)
		{
			store.store(aggregation, metadata);
		}
	}

	@Override
	public void flush()
	{
		checkNotClosed();
		for (EvaluationStore store : stores)
		{
			store.flush();
		}
	}

	@Override
	public void close()
	{
		if (!closed)
		{
			for (EvaluationStore store : stores)
			{
				store.close();
			}
			closed = true;
		}
	}

	private void checkNotClosed()
	{
		if (closed)
		{
			throw new IllegalStateException("Store is closed");
		}
	}
}
