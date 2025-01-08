package dev.rage4j.model;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * The {@code EvaluationAggregation} class represents an immutable map-like
 * structure that stores a collection of evaluation results, with metric names
 * as keys and their corresponding evaluation values as {@code Double}. This
 * class implements the {@code Map} interface but is immutable, meaning it does
 * not support modifications after construction.
 * <p>
 * Instances of this class can only be created using the {@code Builder}
 * pattern.
 */
public class EvaluationAggregation implements Map<String, Double>
{
	private final Map<String, Double> map;
	private static final String IMMUTABLE_MESSAGE = "EvaluationAggregation is immutable.";
	private static final String IMMUTABLE_MAP_MESSAGE = "This map is immutable.";

	private EvaluationAggregation(Map<String, Double> map)
	{
		this.map = Collections.unmodifiableMap(new HashMap<>(map));
	}

	@Override
	public int size()
	{
		return map.size();
	}

	@Override
	public boolean isEmpty()
	{
		return map.isEmpty();
	}

	@Override
	public boolean containsKey(Object key)
	{
		return map.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value)
	{
		return map.containsValue(value);
	}

	@Override
	public Double get(Object key)
	{
		return map.get(key);
	}

	@Override
	@NotNull
	public Set<Entry<String, Double>> entrySet()
	{
		return map.entrySet();
	}

	@Override
	@NotNull
	public Set<String> keySet()
	{
		return map.keySet();
	}

	@Override
	@NotNull
	public Collection<Double> values()
	{
		return map.values();
	}

	@Override
	public Double put(String key, Double value)
	{
		throw new UnsupportedOperationException(IMMUTABLE_MAP_MESSAGE);
	}

	@Override
	public Double remove(Object key)
	{
		throw new UnsupportedOperationException(IMMUTABLE_MESSAGE);
	}

	@Override
	public void putAll(@NotNull Map<? extends String, ? extends Double> m)
	{
		throw new UnsupportedOperationException(IMMUTABLE_MESSAGE);
	}

	@Override
	public void clear()
	{
		throw new UnsupportedOperationException(IMMUTABLE_MESSAGE);
	}

	public static Builder builder()
	{
		return new Builder();
	}

	/**
	 * The {@code Builder} class provides a way to construct an instance of
	 * {@code EvaluationAggregation} by incrementally adding key-value pairs and
	 * then calling the {@code build()} method.
	 */
	public static class Builder
	{
		private final Map<String, Double> map = new HashMap<>();

		/**
		 * Adds a new evaluation metric and its value to the builder.
		 *
		 * @param key
		 *            The metric name.
		 * @param value
		 *            The evaluation value for the metric.
		 */
		public void put(String key, Double value)
		{
			map.put(key, value);
		}

		/**
		 * Builds and returns a new immutable {@code EvaluationAggregation}
		 * instance containing the added metric-value pairs.
		 *
		 * @return A new {@code EvaluationAggregation} instance.
		 */
		public EvaluationAggregation build()
		{
			return new EvaluationAggregation(map);
		}
	}
}
