package dev.rage4j.asserts;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.service.tool.ToolExecution;
import dev.rage4j.model.ToolArguments;
import dev.rage4j.model.ToolCall;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * The {@code ToolExecutionAdapter} class converts the tool executions
 * LangChain4j reports on a {@code Result} into the {@link ToolCall} model
 * Rage4J evaluates.
 */
public final class ToolExecutionAdapter
{
	private ToolExecutionAdapter()
	{
	}

	/**
	 * Converts LangChain4j tool executions into tool calls.
	 *
	 * @param toolExecutions
	 *            The tool executions reported by LangChain4j.
	 * @return The corresponding tool calls, in the same order.
	 */
	public static List<ToolCall> toToolCalls(List<ToolExecution> toolExecutions)
	{
		Objects.requireNonNull(toolExecutions, "toolExecutions");
		return toolExecutions.stream()
			.map(ToolExecutionAdapter::toToolCall)
			.toList();
	}

	/**
	 * Converts a single LangChain4j tool execution into a tool call.
	 *
	 * @param toolExecution
	 *            The tool execution reported by LangChain4j.
	 * @return The corresponding tool call.
	 */
	public static ToolCall toToolCall(ToolExecution toolExecution)
	{
		Objects.requireNonNull(toolExecution, "toolExecution");
		ToolExecutionRequest request = toolExecution.request();
		return ToolCall.of(request.name(), toArguments(request.arguments()));
	}

	private static Map<String, Object> toArguments(String argumentsJson)
	{
		if (argumentsJson == null || argumentsJson.isBlank())
		{
			return Map.of();
		}
		return ToolArguments.fromJson(argumentsJson);
	}
}
