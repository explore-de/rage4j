package dev.rage4j.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ToolCallTest
{
	@Test
	void testOfNameHasNoArguments()
	{
		ToolCall toolCall = ToolCall.of("getWeather");

		assertEquals("getWeather", toolCall.name());
		assertTrue(toolCall.arguments().isEmpty());
	}

	@Test
	void testWithArgumentDoesNotMutateOriginal()
	{
		ToolCall original = ToolCall.of("getWeather");
		ToolCall extended = original.withArgument("city", "Berlin");

		assertTrue(original.arguments().isEmpty());
		assertEquals(Map.of("city", "Berlin"), extended.arguments());
	}

	@Test
	void testWithArgumentsMerges()
	{
		ToolCall toolCall = ToolCall.of("getWeather")
			.withArguments(Map.of("city", "Berlin"))
			.withArgument("day", "tomorrow");

		assertEquals(Map.of("city", "Berlin", "day", "tomorrow"), toolCall.arguments());
	}

	@Test
	void testWithArgumentOverwritesExistingKey()
	{
		ToolCall toolCall = ToolCall.of("getWeather")
			.withArgument("city", "Berlin")
			.withArgument("city", "Hamburg");

		assertEquals(Map.of("city", "Hamburg"), toolCall.arguments());
	}

	@Test
	void testArgumentsAreImmutable()
	{
		ToolCall toolCall = ToolCall.of("getWeather").withArgument("city", "Berlin");

		assertThrows(UnsupportedOperationException.class, () -> toolCall.arguments().put("day", "tomorrow"));
	}

	@Test
	void testNestedArgumentsAreCopiedDefensively()
	{
		Map<String, Object> nestedFilter = new HashMap<>();
		nestedFilter.put("limit", 3);
		Map<String, Object> arguments = new HashMap<>();
		arguments.put("filter", nestedFilter);

		ToolCall toolCall = ToolCall.of("search", arguments);
		nestedFilter.put("limit", 99);

		assertEquals(Map.of("limit", 3), toolCall.arguments().get("filter"));
	}

	@Test
	void testNestedMapArgumentsAreImmutable()
	{
		ToolCall toolCall = ToolCall.of("search", Map.of("filter", new HashMap<>(Map.of("limit", 3))));

		Map<?, ?> filter = (Map<?, ?>)toolCall.arguments().get("filter");
		assertThrows(UnsupportedOperationException.class, () -> ((Map<Object, Object>)filter).put("limit", 99));
	}

	@Test
	void testNestedListArgumentsAreImmutable()
	{
		ToolCall toolCall = ToolCall.of("search", Map.of("tags", new ArrayList<>(List.of("a", "b"))));

		List<?> tags = (List<?>)toolCall.arguments().get("tags");
		assertThrows(UnsupportedOperationException.class, () -> ((List<Object>)tags).add("c"));
	}

	@Test
	void testNameIsRequired()
	{
		assertThrows(NullPointerException.class, () -> ToolCall.of(null));
	}

	@Test
	void testMatchesRequiresEqualNameAndExpectedArgumentsAsSubset()
	{
		ToolCall actual = ToolCall.of("getWeather").withArguments(Map.of("city", "Berlin", "day", "tomorrow"));

		assertTrue(ToolCall.of("getWeather").matches(actual));
		assertTrue(ToolCall.of("getWeather").withArgument("city", "Berlin").matches(actual));
		assertFalse(ToolCall.of("getWeather").withArgument("city", "Hamburg").matches(actual));
		assertFalse(ToolCall.of("sendMail").matches(actual));
	}

	@Test
	void testEqualsAndHashCode()
	{
		EqualsVerifier.forClass(ToolCall.class)
			.suppress(Warning.NULL_FIELDS)
			.verify();
	}
}
