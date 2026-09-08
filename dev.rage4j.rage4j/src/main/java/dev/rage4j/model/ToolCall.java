package dev.rage4j.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * The {@code ToolCall} record represents a single invocation of a tool, either
 * one that a language model actually performed or one that a test expects it to
 * perform.
 * <p>
 * Instances are immutable; {@link #withArgument(String, Object)} and
 * {@link #withArguments(Map)} return new instances with the additional
 * arguments merged in.
 * <p>
 * When used as an expectation, the arguments are matched as a subset of the
 * actual ones, so a {@code ToolCall} without any arguments matches every actual
 * call of the same name.
 *
 * @param name
 *            The name of the tool, as reported by the model.
 * @param arguments
 *            The arguments the tool was called with, keyed by parameter name.
 */
public record ToolCall(String name, Map<String, Object> arguments) implements Serializable
{
	public ToolCall
	{
		Objects.requireNonNull(name, "name");
		Objects.requireNonNull(arguments, "arguments");
		arguments = Collections.unmodifiableMap(new LinkedHashMap<>(arguments));
	}

	/**
	 * Creates a tool call without arguments.
	 *
	 * @param name
	 *            The name of the tool.
	 * @return A new {@code ToolCall} carrying no arguments.
	 */
	public static ToolCall of(String name)
	{
		return new ToolCall(name, Map.of());
	}

	/**
	 * Creates a tool call with the given arguments.
	 *
	 * @param name
	 *            The name of the tool.
	 * @param arguments
	 *            The arguments the tool was called with.
	 * @return A new {@code ToolCall} carrying the given arguments.
	 */
	public static ToolCall of(String name, Map<String, Object> arguments)
	{
		return new ToolCall(name, arguments);
	}

	/**
	 * Returns a copy of this tool call with one additional argument. An
	 * existing argument of the same name is overwritten.
	 *
	 * @param argumentName
	 *            The parameter name of the argument.
	 * @param value
	 *            The argument value, which may be {@code null}.
	 * @return A new {@code ToolCall} with the argument merged in.
	 */
	public ToolCall withArgument(String argumentName, Object value)
	{
		Objects.requireNonNull(argumentName, "argumentName");
		Map<String, Object> merged = new LinkedHashMap<>(arguments);
		merged.put(argumentName, value);
		return new ToolCall(name, merged);
	}

	/**
	 * Returns a copy of this tool call with the given arguments merged in.
	 * Existing arguments of the same name are overwritten.
	 *
	 * @param additionalArguments
	 *            The arguments to merge in.
	 * @return A new {@code ToolCall} with the arguments merged in.
	 */
	public ToolCall withArguments(Map<String, Object> additionalArguments)
	{
		Objects.requireNonNull(additionalArguments, "additionalArguments");
		Map<String, Object> merged = new LinkedHashMap<>(arguments);
		merged.putAll(additionalArguments);
		return new ToolCall(name, merged);
	}

	/**
	 * Checks whether an actual tool call satisfies this expectation. The names
	 * must be equal and every expected argument must be present with an
	 * equivalent value, so an expectation without arguments matches any call of
	 * the same name.
	 *
	 * @param actualToolCall
	 *            The call the language model actually performed.
	 * @return {@code true} if the actual call satisfies this expectation.
	 */
	public boolean matches(ToolCall actualToolCall)
	{
		Objects.requireNonNull(actualToolCall, "actualToolCall");
		return name.equals(actualToolCall.name()) && ToolArguments.matches(arguments, actualToolCall.arguments());
	}
}
