package dev.rage4j.asserts.toolref;

import java.io.Serializable;

/**
 * A serializable reference to a tool method taking 0 argument(s), used to
 * declare an expected tool call by pointing at the method instead of naming it
 * as a string.
 * <p>
 * The functional method returns {@code void} on purpose: a method reference to
 * a value-returning method is compatible with a void function type, so this
 * single interface accepts both value-returning and void tool methods, and no
 * ambiguous overload arises.
 *
 * @param <T>
 *            The type declaring the tool method.
 */
@FunctionalInterface
public interface ToolRef0<T> extends Serializable
{
	void invoke(T tools);
}
