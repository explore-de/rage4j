package dev.rage4j.asserts.exception;

public class Rage4JToolCallException extends RuntimeException
{
	public Rage4JToolCallException(String message)
	{
		super(message);
	}

	public Rage4JToolCallException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public Rage4JToolCallException()
	{
		// empty
	}

}
