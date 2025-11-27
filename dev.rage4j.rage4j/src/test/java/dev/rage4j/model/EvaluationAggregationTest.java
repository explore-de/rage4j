package dev.rage4j.model;

import dev.rage4j.LoggingTestWatcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@ExtendWith(LoggingTestWatcher.class)
class EvaluationAggregationTest
{
	@Test
	void testEqualsReflexive()
	{
		EvaluationAggregation aggregation = new EvaluationAggregation();
		aggregation.put("metric", 1.0);
		assertEquals(aggregation, aggregation);
	}

	@Test
	void testEqualsSymmetric()
	{
		Sample sample = Sample.builder().withQuestion("q").build();
		EvaluationAggregation a = new EvaluationAggregation(sample);
		a.put("metric", 1.0);
		EvaluationAggregation b = new EvaluationAggregation(sample);
		b.put("metric", 1.0);

		assertEquals(a, b);
		assertEquals(b, a);
	}

	@Test
	void testEqualsWithDifferentMapContent()
	{
		EvaluationAggregation a = new EvaluationAggregation();
		a.put("metric", 1.0);
		EvaluationAggregation b = new EvaluationAggregation();
		b.put("metric", 2.0);

		assertNotEquals(a, b);
	}

	@Test
	void testEqualsWithDifferentSample()
	{
		Sample sample1 = Sample.builder().withQuestion("q1").build();
		Sample sample2 = Sample.builder().withQuestion("q2").build();
		EvaluationAggregation a = new EvaluationAggregation(sample1);
		a.put("metric", 1.0);
		EvaluationAggregation b = new EvaluationAggregation(sample2);
		b.put("metric", 1.0);

		assertNotEquals(a, b);
	}

	@Test
	void testEqualsWithNullSample()
	{
		EvaluationAggregation a = new EvaluationAggregation();
		a.put("metric", 1.0);
		EvaluationAggregation b = new EvaluationAggregation();
		b.put("metric", 1.0);

		assertEquals(a, b);
	}

	@Test
	void testEqualsWithNull()
	{
		EvaluationAggregation aggregation = new EvaluationAggregation();
		assertNotEquals(null, aggregation);
	}

	@Test
	void testEqualsWithDifferentClass()
	{
		EvaluationAggregation aggregation = new EvaluationAggregation();
		assertNotEquals("not an aggregation", aggregation);
	}

	@Test
	void testHashCodeConsistency()
	{
		Sample sample = Sample.builder().withQuestion("q").build();
		EvaluationAggregation aggregation = new EvaluationAggregation(sample);
		aggregation.put("metric", 1.0);

		int hashCode1 = aggregation.hashCode();
		int hashCode2 = aggregation.hashCode();

		assertEquals(hashCode1, hashCode2);
	}

	@Test
	void testHashCodeEquality()
	{
		Sample sample = Sample.builder().withQuestion("q").build();
		EvaluationAggregation a = new EvaluationAggregation(sample);
		a.put("metric", 1.0);
		EvaluationAggregation b = new EvaluationAggregation(sample);
		b.put("metric", 1.0);

		assertEquals(a.hashCode(), b.hashCode());
	}
}
