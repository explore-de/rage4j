package dev.rage4j.asserts.toolref;

import dev.langchain4j.agent.tool.Tool;
import dev.rage4j.asserts.exception.Rage4JToolCallException;

import java.io.Serializable;
import java.lang.reflect.InaccessibleObjectException;
import java.lang.invoke.MethodType;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * The {@code ToolNameResolver} class turns a method reference such as
 * {@code WeatherTools::getWeather} into the tool name the language model
 * reports.
 * <p>
 * Resolving the name from the method rather than from a string keeps
 * expectations refactoring-safe and honours a {@code @Tool(name = "...")}
 * override, which a hand-written string would silently get wrong.
 */
public final class ToolNameResolver
{
	private ToolNameResolver()
	{
	}

	/**
	 * Resolves the tool name of the referenced method.
	 *
	 * @param toolReference
	 *            A method reference pointing at a {@code @Tool} annotated
	 *            method.
	 * @return The name the tool is registered under.
	 * @throws Rage4JToolCallException
	 *             if the reference cannot be read or does not point at a
	 *             {@code @Tool} annotated method.
	 */
	public static String resolve(Serializable toolReference)
	{
		Objects.requireNonNull(toolReference, "toolReference");
		SerializedLambda serializedLambda = serializedLambda(toolReference);
		Method toolMethod = toolMethod(serializedLambda, toolReference.getClass().getClassLoader());
		Tool tool = toolMethod.getAnnotation(Tool.class);
		if (tool == null)
		{
			throw new Rage4JToolCallException(toolMethod + " is not annotated with @Tool and can therefore not be used as an expected tool call");
		}
		return tool.name().isEmpty() ? toolMethod.getName() : tool.name();
	}

	private static SerializedLambda serializedLambda(Serializable toolReference)
	{
		try
		{
			Method writeReplace = toolReference.getClass().getDeclaredMethod("writeReplace");
			writeReplace.setAccessible(true);
			if (writeReplace.invoke(toolReference) instanceof SerializedLambda serializedLambda)
			{
				return serializedLambda;
			}
			throw new Rage4JToolCallException("Expected a method reference to a @Tool method, but got " + toolReference.getClass());
		}
		catch (ReflectiveOperationException | InaccessibleObjectException e)
		{
			throw new Rage4JToolCallException("Could not read the tool method reference. Expected tool calls must be declared as a method reference such as WeatherTools::getWeather, and under JPMS the module declaring the tool class must open its package.", e);
		}
	}

	private static Method toolMethod(SerializedLambda serializedLambda, ClassLoader classLoader)
	{
		String declaringClassName = serializedLambda.getImplClass().replace('/', '.');
		try
		{
			Class<?> declaringClass = Class.forName(declaringClassName, false, classLoader);
			MethodType methodType = MethodType.fromMethodDescriptorString(serializedLambda.getImplMethodSignature(), classLoader);
			return declaringClass.getDeclaredMethod(serializedLambda.getImplMethodName(), methodType.parameterArray());
		}
		catch (ReflectiveOperationException | IllegalArgumentException | TypeNotPresentException e)
		{
			throw new Rage4JToolCallException("Could not resolve the referenced tool method " + declaringClassName + "#" + serializedLambda.getImplMethodName(), e);
		}
	}
}
