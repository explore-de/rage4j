package dev.rage4j.persist;

import java.time.Instant;

/**
 * Metadata associated with an evaluation record. Contains information about the
 * test context where the evaluation was performed.
 */
public record RecordMetadata(String testClass, String testMethod, Instant timestamp)
{

	/**
	 * Creates metadata with only the current timestamp.
	 *
	 * @return A new RecordMetadata with null test class and method, and the
	 *         current timestamp.
	 */
	public static RecordMetadata now()
	{
		return new RecordMetadata(null, null, Instant.now());
	}

	/**
	 * Creates metadata with the specified test class and method, and the
	 * current timestamp.
	 *
	 * @param testClass
	 *            The fully qualified name of the test class.
	 * @param testMethod
	 *            The name of the test method.
	 * @return A new RecordMetadata with the specified values.
	 */
	public static RecordMetadata of(String testClass, String testMethod)
	{
		return new RecordMetadata(testClass, testMethod, Instant.now());
	}
}
