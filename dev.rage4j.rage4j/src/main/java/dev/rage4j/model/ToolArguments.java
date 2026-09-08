package dev.rage4j.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * The {@code ToolArguments} class provides the parsing and comparison of tool
 * call arguments.
 * <p>
 * Both the arguments reported by a language model and the ones a test declares
 * as expected pass through this class, so that they are always compared in the
 * same representation.
 */
public final class ToolArguments
{
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private ToolArguments()
	{
	}

	/**
	 * Parses a JSON object into an argument map. Language models report tool
	 * call arguments as a JSON object, so this is the canonical way to turn a
	 * recorded call into a comparable map.
	 *
	 * @param json
	 *            A JSON object holding the arguments keyed by parameter name.
	 * @return The parsed arguments.
	 * @throws IllegalArgumentException
	 *             if the input is not valid JSON or not a JSON object.
	 */
	public static Map<String, Object> fromJson(String json)
	{
		Objects.requireNonNull(json, "json");
		try
		{
			JsonNode node = OBJECT_MAPPER.readTree(json);
			if (!node.isObject())
			{
				throw new IllegalArgumentException("Tool arguments must be a JSON object but were: " + json);
			}
			return OBJECT_MAPPER.convertValue(node, new TypeReference<LinkedHashMap<String, Object>>()
			{
			});
		}
		catch (JsonProcessingException e)
		{
			throw new IllegalArgumentException("Tool arguments are not valid JSON: " + json, e);
		}
	}

	/**
	 * Checks whether every expected argument is present in the actual arguments
	 * with an equivalent value. Additional actual arguments are ignored, so an
	 * empty expectation matches any call.
	 * <p>
	 * Top level numbers are compared by numeric value rather than by type,
	 * because the same argument arrives as an {@code Integer} from a JSON
	 * payload and as a {@code Long} from a hand-written expectation. Values
	 * nested inside a map or list are compared with {@code equals}, so numbers
	 * within them must also match in type.
	 *
	 * @param expected
	 *            The arguments the call is expected to carry.
	 * @param actual
	 *            The arguments the call actually carried.
	 * @return {@code true} if the actual arguments satisfy the expectation.
	 */
	public static boolean matches(Map<String, Object> expected, Map<String, Object> actual)
	{
		Objects.requireNonNull(expected, "expected");
		Objects.requireNonNull(actual, "actual");
		for (Map.Entry<String, Object> expectedArgument : expected.entrySet())
		{
			if (!actual.containsKey(expectedArgument.getKey()))
			{
				return false;
			}
			if (!valuesMatch(expectedArgument.getValue(), actual.get(expectedArgument.getKey())))
			{
				return false;
			}
		}
		return true;
	}

	private static boolean valuesMatch(Object expected, Object actual)
	{
		if (expected instanceof Number expectedNumber && actual instanceof Number actualNumber)
		{
			return toBigDecimal(expectedNumber).compareTo(toBigDecimal(actualNumber)) == 0;
		}
		return Objects.equals(expected, actual);
	}

	private static BigDecimal toBigDecimal(Number number)
	{
		return new BigDecimal(number.toString());
	}
}
