package dev.rage4j.persist.junit5;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Generates a self-contained HTML report from the {@code EvaluationStore}
 * injected into the annotated test class.
 *
 * <p>Archive the configured file as a Jenkins HTML artifact after the test run.</p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@ExtendWith(Rage4jPersistExtension.class)
public @interface Rage4jHtmlReport
{
	/**
	 * The generated HTML report. Defaults to a conventional Maven target path.
	 *
	 * @return the report file path
	 */
	String file() default "target/rage4j-report.html";
}
