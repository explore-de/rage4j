package dev.rage4j.config;

import dev.rage4j.LoggingTestWatcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(LoggingTestWatcher.class)
class ConfigFactoryTest
{
	@Test
	void testGetConfigReturnsSingletonInstance()
	{
		AppConfig first = ConfigFactory.getConfig();
		AppConfig second = ConfigFactory.getConfig();

		assertSame(first, second);
	}

	@Test
	void testGetConfigLoadsModelFromEnvOrProperty()
	{
		AppConfig config = ConfigFactory.getConfig();
		String expectedModel = System.getenv().getOrDefault("OPEN_AI_MODEL", "gpt-5.2-2025-12-11");

		assertEquals(expectedModel, config.OPEN_AI_MODEL());
	}

	@Test
	void testGetConfigLoadsEmbeddingModelFromEnvOrProperty()
	{
		AppConfig config = ConfigFactory.getConfig();
		String expectedEmbedding = System.getenv().getOrDefault("OPEN_AI_EMBEDDING_MODEL", "text-embedding-3-large");

		assertEquals(expectedEmbedding, config.OPEN_AI_EMBEDDING_MODEL());
	}

	@Test
	void testGetConfigLoadsOpenAiKeyFromEnvOrDefault()
	{
		AppConfig config = ConfigFactory.getConfig();
		String expectedKey = System.getenv().getOrDefault("OPEN_AI_KEY", "");

		assertEquals(expectedKey, config.OPEN_AI_KEY());
	}

	@Test
	void testEnvOrPropUsesMappedPropertyWhenEnvIsMissing() throws Exception
	{
		Properties props = new Properties();
		props.setProperty("config.factory.test.value", "from-properties");

		String resolved = invokeEnvOrProp("CONFIG_FACTORY_TEST_VALUE", props, "fallback");

		assertEquals("from-properties", resolved);
	}

	@Test
	void testEnvOrPropUsesDefaultWhenEnvAndPropertyAreMissing() throws Exception
	{
		String resolved = invokeEnvOrProp("CONFIG_FACTORY_TEST_MISSING", new Properties(), "fallback");

		assertEquals("fallback", resolved);
	}

	@Test
	void testValidateNoSecretsThrowsWhenOpenAiKeyIsInProperties()
	{
		Properties props = new Properties();
		props.setProperty("open.ai.key", "dont-commit-secrets");

		IllegalStateException exception = assertThrows(IllegalStateException.class, () -> invokeValidateNoSecrets(props));
		assertEquals(
			"WARNING: Secret-Key 'open.ai.key' in application.properties found! Only allowed as env key: OPEN_AI_KEY",
			exception.getMessage()
		);
	}

	@Test
	void testValidateNoSecretsAllowsNonSecretProperties()
	{
		Properties props = new Properties();
		props.setProperty("open.ai.model", "gpt-5.2-2025-12-11");

		assertDoesNotThrow(() -> invokeValidateNoSecrets(props));
	}

	private static String invokeEnvOrProp(String envKey, Properties props, String defaultValue) throws Exception
	{
		Method method = ConfigFactory.class.getDeclaredMethod("envOrProp", String.class, Properties.class, String.class);
		method.setAccessible(true);
		return (String) method.invoke(null, envKey, props, defaultValue);
	}

	private static void invokeValidateNoSecrets(Properties props)
	{
		try {
			Method method = ConfigFactory.class.getDeclaredMethod("validateNoSecrets", Properties.class);
			method.setAccessible(true);
			method.invoke(null, props);
		} catch (InvocationTargetException e) {
			Throwable cause = e.getCause();
			if (cause instanceof RuntimeException runtimeException) {
				throw runtimeException;
			}
			throw new RuntimeException(cause);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}

