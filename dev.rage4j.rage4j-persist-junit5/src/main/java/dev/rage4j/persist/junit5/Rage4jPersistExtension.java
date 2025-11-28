package dev.rage4j.persist.junit5;

import java.lang.reflect.Method;
import java.nio.file.Path;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import dev.rage4j.persist.EvaluationStore;
import dev.rage4j.persist.Rage4jPersist;
import dev.rage4j.persist.RecordMetadata;

/**
 * JUnit 5 extension that manages {@link EvaluationStore} lifecycle for test classes.
 *
 * <p>
 * This extension:
 * </p>
 * <ul>
 * <li>Creates an {@link EvaluationStore} before all tests in a class</li>
 * <li>Optionally configures it as the global store</li>
 * <li>Injects the store into test methods that declare it as a parameter</li>
 * <li>Sets up test metadata context for each test method</li>
 * <li>Closes the store after all tests complete</li>
 * </ul>
 *
 * <p>
 * Use with the {@link Rage4jPersistConfig} annotation:
 * </p>
 *
 * <pre>
 * &#64;Rage4jPersistConfig(file = "target/evaluations.jsonl")
 * class MyTest {
 *
 *     &#64;Test
 *     void testEvaluation(EvaluationStore store) {
 *         // ...
 *     }
 * }
 * </pre>
 */
public class Rage4jPersistExtension implements BeforeAllCallback, AfterAllCallback, BeforeEachCallback, ParameterResolver
{

	private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(Rage4jPersistExtension.class);

	private static final String STORE_KEY = "evaluationStore";
	private static final String CONFIG_KEY = "config";

	@Override
	public void beforeAll(ExtensionContext context) throws Exception
	{
		Rage4jPersistConfig config = findConfig(context);
		EvaluationStore store = createStore(config);

		ExtensionContext.Store extensionStore = context.getStore(NAMESPACE);
		extensionStore.put(STORE_KEY, new CloseableStoreWrapper(store));
		extensionStore.put(CONFIG_KEY, config);

		if (config.configureGlobal())
		{
			Rage4jPersist.configure(store);
		}
	}

	@Override
	public void beforeEach(ExtensionContext context)
	{
		String testClass = context.getTestClass().map(Class::getName).orElse(null);
		String testMethod = context.getTestMethod().map(Method::getName).orElse(null);

		RecordMetadata metadata = RecordMetadata.of(testClass, testMethod);
		Rage4jPersist.setTestContext(metadata);
	}

	@Override
	public void afterAll(ExtensionContext context)
	{
		Rage4jPersistConfig config = context.getStore(NAMESPACE).get(CONFIG_KEY, Rage4jPersistConfig.class);
		if (config != null && config.configureGlobal())
		{
			Rage4jPersist.clearConfiguration();
		}
		// CloseableStoreWrapper will be auto-closed by JUnit's Store mechanism
	}

	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException
	{
		return EvaluationStore.class.isAssignableFrom(parameterContext.getParameter().getType());
	}

	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException
	{
		CloseableStoreWrapper wrapper = extensionContext.getStore(NAMESPACE).get(STORE_KEY, CloseableStoreWrapper.class);
		if (wrapper == null)
		{
			throw new ParameterResolutionException("EvaluationStore not initialized. Did you add @Rage4jPersistConfig to your test class?");
		}
		return wrapper.getStore();
	}

	private Rage4jPersistConfig findConfig(ExtensionContext context)
	{
		return context.getTestClass().map(this::findConfigOnClass).orElseGet(this::defaultConfig);
	}

	private Rage4jPersistConfig findConfigOnClass(Class<?> testClass)
	{
		Rage4jPersistConfig config = testClass.getAnnotation(Rage4jPersistConfig.class);
		if (config != null)
		{
			return config;
		}
		// Check parent classes
		Class<?> superclass = testClass.getSuperclass();
		if (superclass != null && superclass != Object.class)
		{
			return findConfigOnClass(superclass);
		}
		return defaultConfig();
	}

	private Rage4jPersistConfig defaultConfig()
	{
		@Rage4jPersistConfig
		class DefaultHolder
		{
		}
		return DefaultHolder.class.getAnnotation(Rage4jPersistConfig.class);
	}

	private EvaluationStore createStore(Rage4jPersistConfig config)
	{
		Path path = Path.of(config.file());
		Class<? extends EvaluationStore> storeClass = config.storeClass();

		try
		{
			return storeClass.getConstructor(Path.class).newInstance(path);
		}
		catch (ReflectiveOperationException e)
		{
			throw new IllegalArgumentException(
				"Failed to instantiate store class " + storeClass.getName() + ". " + "Ensure it has a public constructor that accepts a Path parameter.",
				e);
		}
	}

	/**
	 * Wrapper to make EvaluationStore closeable for JUnit's Store mechanism.
	 */
	private static class CloseableStoreWrapper implements ExtensionContext.Store.CloseableResource
	{

		private final EvaluationStore store;

		CloseableStoreWrapper(EvaluationStore store)
		{
			this.store = store;
		}

		EvaluationStore getStore()
		{
			return store;
		}

		@Override
		public void close() throws Throwable
		{
			store.close();
		}
	}
}
