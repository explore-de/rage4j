package dev.rage4j.model;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ToolArgumentsTest
{
	@Test
	void testFromJsonParsesFlatObject()
	{
		Map<String, Object> arguments = ToolArguments.fromJson("{\"city\": \"Berlin\", \"day\": \"tomorrow\"}");

		assertEquals(Map.of("city", "Berlin", "day", "tomorrow"), arguments);
	}

	@Test
	void testFromJsonParsesNestedObject()
	{
		Map<String, Object> arguments = ToolArguments.fromJson("{\"filter\": {\"country\": \"DE\"}}");

		assertEquals(Map.of("country", "DE"), arguments.get("filter"));
	}

	@Test
	void testFromJsonParsesEmptyObject()
	{
		assertTrue(ToolArguments.fromJson("{}").isEmpty());
	}

	@Test
	void testFromJsonRejectsMalformedJson()
	{
		assertThrows(IllegalArgumentException.class, () -> ToolArguments.fromJson("{\"city\": "));
	}

	@Test
	void testFromJsonRejectsNonObjectJson()
	{
		assertThrows(IllegalArgumentException.class, () -> ToolArguments.fromJson("[\"Berlin\"]"));
	}

	@Test
	void testFromJsonRejectsNull()
	{
		assertThrows(NullPointerException.class, () -> ToolArguments.fromJson(null));
	}

	@Test
	void testMatchesTreatsEquivalentNumbersAsEqual()
	{
		assertTrue(ToolArguments.matches(Map.of("count", 3), Map.of("count", 3L)));
		assertTrue(ToolArguments.matches(Map.of("ratio", 1.50), Map.of("ratio", 1.5)));
	}

	@Test
	void testNestedValuesAreComparedWithoutNumericNormalisation()
	{
		assertTrue(ToolArguments.matches(Map.of("filter", Map.of("limit", 3)), Map.of("filter", Map.of("limit", 3))));
		assertFalse(ToolArguments.matches(Map.of("filter", Map.of("limit", 3)), Map.of("filter", Map.of("limit", 3L))));
	}

	@Test
	void testMatchesRequiresExpectedEntriesToBePresent()
	{
		assertTrue(ToolArguments.matches(Map.of("city", "Berlin"), Map.of("city", "Berlin", "day", "tomorrow")));
		assertTrue(ToolArguments.matches(Map.of(), Map.of("city", "Berlin")));
	}

	@Test
	void testMatchesFailsOnDifferentValue()
	{
		assertFalse(ToolArguments.matches(Map.of("city", "Berlin"), Map.of("city", "Hamburg")));
		assertFalse(ToolArguments.matches(Map.of("city", "Berlin"), Map.of("day", "tomorrow")));
	}
}
