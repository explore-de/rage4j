package dev.rage4j.persist.junit5;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtendWith;

import dev.rage4j.persist.EvaluationStore;
import dev.rage4j.persist.store.JsonLinesStore;

/**
 * Configures Rage4j evaluation persistence for a test class. When applied,
 * evaluations will be automatically persisted to the specified file.
 *
 * <p>
 * Example usage:
 * </p>
 *
 * <pre>
 * &#64;Rage4jPersistConfig(file = "target/evaluations.jsonl")
 * class MyEvaluationTest {
 *
 *     &#64;Test
 *     void testEvaluation(EvaluationStore store) {
 *         // store is injected and ready to use
 *         EvaluationAggregation aggregation = ...;
 *         store.store(aggregation);
 *     }
 * }
 * </pre>
 *
 * <p>
 * Custom store implementation:
 * </p>
 *
 * <pre>
 * &#64;Rage4jPersistConfig(file = "target/custom.dat", storeClass = MyCustomStore.class)
 * class MyCustomTest {
 *     // MyCustomStore must have a constructor that accepts a Path
 * }
 * </pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@ExtendWith(Rage4jPersistExtension.class)
public @interface Rage4jPersistConfig
{

	/**
	 * The file path where evaluations will be stored. Defaults to
	 * "target/evaluations.jsonl".
	 *
	 * @return the output file path
	 */
	String file() default "target/evaluations.jsonl";

	/**
	 * The store implementation class to use. Must have a constructor that accepts
	 * a {@link java.nio.file.Path}. Defaults to {@link JsonLinesStore}.
	 *
	 * @return the store class
	 */
	Class<? extends EvaluationStore> storeClass() default JsonLinesStore.class;

	/**
	 * Whether to configure the store as the global static store via
	 * {@link dev.rage4j.persist.Rage4jPersist#configure(dev.rage4j.persist.EvaluationStore)}.
	 *
	 * @return true to configure as global store
	 */
	boolean configureGlobal() default true;
}
