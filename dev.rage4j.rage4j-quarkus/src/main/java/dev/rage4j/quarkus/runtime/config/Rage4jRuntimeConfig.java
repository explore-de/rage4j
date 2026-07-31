package dev.rage4j.quarkus.runtime.config;

import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

/**
 * Quarkus configuration root for the Rage4J extension.
 */
@ConfigMapping(prefix = "quarkus.rage4j")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface Rage4jRuntimeConfig
{
	/**
	 * Enables evaluation mode for the produced RageAssert bean.
	 */
	@WithDefault("false")
	boolean evaluationMode();

	/**
	 * Optional CDI bean name, bean class name, or Quarkus
	 * LangChain4j @ModelName value used to select the ChatModel.
	 */
	Optional<String> chatModelBean();

	/**
	 * Optional CDI bean name, bean class name, or Quarkus
	 * LangChain4j @ModelName value used to select the EmbeddingModel.
	 */
	Optional<String> embeddingModelBean();

	/**
	 * Evaluation result persistence settings.
	 */
	PersistenceConfig persistence();

	/**
	 * Evaluation result persistence settings.
	 */
	interface PersistenceConfig
	{
		/**
		 * Enables the default JSONL store.
		 */
		@WithDefault("false")
		boolean enabled();

		/**
		 * Path of the JSONL file.
		 */
		@WithDefault("target/evaluations.jsonl")
		String file();
	}
}
