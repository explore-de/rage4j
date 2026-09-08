package dev.rage4j.asserts;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.invocation.InvocationContext;
import dev.langchain4j.service.tool.ToolExecution;
import dev.langchain4j.service.tool.ToolExecutionResult;
import dev.rage4j.model.ToolCall;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ToolExecutionAdapterTest
{
	@Test
	void testConvertsNameAndJsonArguments()
	{
		ToolCall toolCall = ToolExecutionAdapter.toToolCall(toolExecution("getWeather", "{\"city\": \"Berlin\", \"day\": \"tomorrow\"}"));

		assertEquals("getWeather", toolCall.name());
		assertEquals(Map.of("city", "Berlin", "day", "tomorrow"), toolCall.arguments());
	}

	@Test
	void testConvertsToolWithoutArguments()
	{
		assertTrue(ToolExecutionAdapter.toToolCall(toolExecution("getTime", "{}")).arguments().isEmpty());
		assertTrue(ToolExecutionAdapter.toToolCall(toolExecution("getTime", "")).arguments().isEmpty());
		assertTrue(ToolExecutionAdapter.toToolCall(toolExecution("getTime", null)).arguments().isEmpty());
	}

	@Test
	void testPreservesOrderOfToolExecutions()
	{
		List<ToolCall> toolCalls = ToolExecutionAdapter.toToolCalls(List.of(
			toolExecution("findCustomer", "{}"),
			toolExecution("cancelBooking", "{}")));

		assertEquals(List.of("findCustomer", "cancelBooking"), toolCalls.stream().map(ToolCall::name).toList());
	}

	private ToolExecution toolExecution(String name, String arguments)
	{
		return ToolExecution.builder()
			.request(ToolExecutionRequest.builder().name(name).arguments(arguments).build())
			.result(ToolExecutionResult.builder().resultText("ok").build())
			.invocationContext(InvocationContext.builder().build())
			.build();
	}
}
