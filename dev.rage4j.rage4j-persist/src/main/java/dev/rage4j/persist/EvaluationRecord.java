package dev.rage4j.persist;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import dev.rage4j.model.EvaluationAggregation;
import dev.rage4j.model.Sample;

/**
 * Represents a single evaluation record with a unique ID, sample data, metrics,
 * and metadata. This is the internal representation used for persistence.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"id", "timestamp", "testClass", "testMethod", "sample", "metrics"})
public record EvaluationRecord(String id, @JsonIgnore Sample sample, Map<String, Double> metrics,
	@JsonIgnore RecordMetadata metadata)
{

	@JsonProperty("timestamp")
	public String timestamp()
	{
		return metadata.timestamp().toString();
	}

	@JsonProperty("testClass")
	public String testClass()
	{
		return metadata.testClass();
	}

	@JsonProperty("testMethod")
	public String testMethod()
	{
		return metadata.testMethod();
	}

	@JsonProperty("sample")
	public Map<String, String> sampleMap()
	{
		Map<String, String> map = new LinkedHashMap<>();
		if (sample.hasQuestion())
		{
			map.put("question", sample.getQuestion());
		}
		if (sample.hasAnswer())
		{
			map.put("answer", sample.getAnswer());
		}
		if (sample.hasGroundTruth())
		{
			map.put("groundTruth", sample.getGroundTruth());
		}
		if (sample.hasContext())
		{
			map.put("context", sample.getContext());
		}
		return map;
	}

	/**
	 * Creates an EvaluationRecord from an EvaluationAggregation and metadata.
	 *
	 * @param aggregation
	 *            The evaluation aggregation containing sample and metrics.
	 * @param metadata
	 *            The metadata for this record.
	 * @return A new EvaluationRecord with a generated UUID.
	 * @throws IllegalArgumentException
	 *             if the aggregation has no associated Sample.
	 */
	public static EvaluationRecord from(EvaluationAggregation aggregation, RecordMetadata metadata)
	{
		Sample sample = aggregation.getSample()
			.orElseThrow(() -> new IllegalArgumentException("EvaluationAggregation must have an associated Sample"));

		return new EvaluationRecord(UUID.randomUUID().toString(), sample, Map.copyOf(aggregation), metadata);
	}
}
