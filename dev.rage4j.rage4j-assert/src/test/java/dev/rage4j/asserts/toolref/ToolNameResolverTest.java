package dev.rage4j.asserts.toolref;

import dev.rage4j.asserts.exception.Rage4JToolCallException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ToolNameResolverTest
{
	@Test
	void testResolvesMethodNameOfValueReturningTool()
	{
		assertEquals("getWeather", ToolNameResolver.resolve((ToolRef2<WeatherTools, String, String>)WeatherTools::getWeather));
	}

	@Test
	void testResolvesMethodNameOfToolWithoutArguments()
	{
		assertEquals("getTime", ToolNameResolver.resolve((ToolRef0<WeatherTools>)WeatherTools::getTime));
	}

	@Test
	void testResolvesExplicitToolNameOfVoidTool()
	{
		assertEquals("send_alert", ToolNameResolver.resolve((ToolRef1<WeatherTools, String>)WeatherTools::sendAlert));
	}

	@Test
	void testRejectsMethodWithoutToolAnnotation()
	{
		Rage4JToolCallException exception = assertThrows(Rage4JToolCallException.class,
			() -> ToolNameResolver.resolve((ToolRef1<WeatherTools, String>)WeatherTools::notATool));

		assertTrue(exception.getMessage().contains("@Tool"));
	}

	@Test
	void testRejectsNonMethodReferenceLambda()
	{
		assertThrows(Rage4JToolCallException.class,
			() -> ToolNameResolver.resolve((ToolRef0<WeatherTools>)tools -> tools.getTime()));
	}
}
