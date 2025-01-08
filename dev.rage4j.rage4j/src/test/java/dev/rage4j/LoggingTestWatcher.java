package dev.rage4j;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.slf4j.MDC;

public class LoggingTestWatcher implements TestWatcher
{
	@Override
	public void testSuccessful(ExtensionContext context)
	{
		MDC.put("testName", context.getDisplayName());
	}

	@Override
	public void testFailed(ExtensionContext context, Throwable cause)
	{
		MDC.remove("testName");
	}
}