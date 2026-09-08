package dev.rage4j.asserts.toolref;

import java.io.Serializable;

/**
 * A serializable reference to a tool method taking 4 argument(s), used to
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
 * @param <A>
 *            The type of the first tool argument.
 * @param <B>
 *            The type of the second tool argument.
 * @param <C>
 *            The type of the third tool argument.
 * @param <D>
 *            The type of the fourth tool argument.
 */
@FunctionalInterface
public interface ToolRef4<T, A, B, C, D> extends Serializable
{
	void invoke(T tools, A a, B b, C c, D d);
}
