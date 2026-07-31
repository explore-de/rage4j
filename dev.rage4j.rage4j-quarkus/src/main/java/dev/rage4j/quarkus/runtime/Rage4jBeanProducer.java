package dev.rage4j.quarkus.runtime;

import java.lang.annotation.Annotation;
import java.nio.file.Path;
import java.util.Optional;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.Instance.Handle;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.context.ApplicationScoped;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.rage4j.asserts.RageAssert;
import dev.rage4j.persist.EvaluationStore;
import dev.rage4j.persist.store.JsonLinesStore;
import dev.rage4j.quarkus.runtime.config.Rage4jRuntimeConfig;
import io.quarkus.arc.DefaultBean;

/**
 * Produces the Rage4J beans used by Quarkus tests.
 *
 * <p>
 * Models remain owned by the application or by another Quarkus extension. This
 * producer only adapts those CDI-managed models to Rage4J, so provider-specific
 * configuration and credentials are not duplicated.
 * </p>
 */
@ApplicationScoped
public class Rage4jBeanProducer
{
	private static final String MODEL_NAME_QUALIFIER = "io.quarkiverse.langchain4j.ModelName";

	private final Instance<ChatModel> defaultChatModels;
	private final Instance<ChatModel> allChatModels;
	private final Instance<EmbeddingModel> defaultEmbeddingModels;
	private final Instance<EmbeddingModel> allEmbeddingModels;
	private final Rage4jRuntimeConfig config;

	private EvaluationStore evaluationStore;

	public Rage4jBeanProducer(Instance<ChatModel> defaultChatModels, @Any Instance<ChatModel> allChatModels,
		Instance<EmbeddingModel> defaultEmbeddingModels, @Any Instance<EmbeddingModel> allEmbeddingModels, Rage4jRuntimeConfig config)
	{
		this.defaultChatModels = defaultChatModels;
		this.allChatModels = allChatModels;
		this.defaultEmbeddingModels = defaultEmbeddingModels;
		this.allEmbeddingModels = allEmbeddingModels;
		this.config = config;
	}

	/**
	 * Provides a default {@link RageAssert} bean. Applications can replace this
	 * bean with their own producer when they need custom model routing.
	 *
	 * @return the configured RageAssert instance
	 */
	@Produces
	@ApplicationScoped
	@DefaultBean
	public RageAssert rageAssert()
	{
		ChatModel chatModel = resolveModel(defaultChatModels, allChatModels, config.chatModelBean(), "ChatModel", true);
		EmbeddingModel embeddingModel = resolveModel(defaultEmbeddingModels, allEmbeddingModels, config.embeddingModelBean(), "EmbeddingModel", false);

		RageAssert rageAssert = new RageAssert(chatModel, embeddingModel);
		return config.evaluationMode() ? rageAssert.withEvaluationMode() : rageAssert;
	}

	/**
	 * Provides the configured evaluation store. Persistence is disabled by
	 * default, in which case the injected store is a no-op.
	 *
	 * @return the configured evaluation store
	 */
	@Produces
	@ApplicationScoped
	@DefaultBean
	public EvaluationStore evaluationStore()
	{
		if (evaluationStore == null)
		{
			evaluationStore = config.persistence().enabled()
				? new JsonLinesStore(Path.of(config.persistence().file()))
				: new NoopEvaluationStore();
		}
		return evaluationStore;
	}

	@PreDestroy
	void closeEvaluationStore()
	{
		if (evaluationStore != null)
		{
			evaluationStore.close();
		}
	}

	private static <T> T resolveModel(Instance<T> defaultModels, Instance<T> allModels, Optional<String> configuredBeanName,
		String modelType, boolean required)
	{
		if (configuredBeanName.isPresent() && !configuredBeanName.get().isBlank())
		{
			String beanName = configuredBeanName.get();
			for (Handle<T> handle : allModels.handles())
			{
				try
				{
					Bean<T> bean = handle.getBean();
					if (matchesBean(bean, beanName))
					{
						return handle.get();
					}
				}
				finally
				{
					handle.close();
				}
			}
			throw new IllegalStateException("No " + modelType + " CDI bean named '" + beanName + "' was found.");
		}

		if (defaultModels.isResolvable())
		{
			return defaultModels.get();
		}
		if (allModels.isResolvable())
		{
			return allModels.get();
		}
		if (allModels.isUnsatisfied())
		{
			if (required)
			{
				throw new IllegalStateException("No " + modelType + " CDI bean is available. Configure a LangChain4j model or provide one with CDI.");
			}
			return null;
		}

		throw new IllegalStateException("More than one " + modelType
			+ " CDI bean is available. Set quarkus.rage4j." + modelType.replace("Model", "").toLowerCase()
			+ "-model-bean to select one.");
	}

	private static boolean matchesBean(Bean<?> bean, String configuredName)
	{
		if (configuredName.equals(bean.getName()) || configuredName.equals(bean.getBeanClass().getName()))
		{
			return true;
		}

		for (Annotation qualifier : bean.getQualifiers())
		{
			if (MODEL_NAME_QUALIFIER.equals(qualifier.annotationType().getName()))
			{
				try
				{
					Object modelName = qualifier.annotationType().getMethod("value").invoke(qualifier);
					return configuredName.equals(modelName);
				}
				catch (ReflectiveOperationException e)
				{
					throw new IllegalStateException("Unable to read the LangChain4j @ModelName qualifier.", e);
				}
			}
		}
		return false;
	}
}
