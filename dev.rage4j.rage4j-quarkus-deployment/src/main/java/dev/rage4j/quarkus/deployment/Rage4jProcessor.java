package dev.rage4j.quarkus.deployment;

import dev.rage4j.quarkus.runtime.Rage4jBeanProducer;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;

/**
 * Registers the Rage4J CDI integration during Quarkus augmentation.
 */
public class Rage4jProcessor
{
	private static final String FEATURE = "rage4j";

	@BuildStep
	FeatureBuildItem feature()
	{
		return new FeatureBuildItem(FEATURE);
	}

	@BuildStep
	AdditionalBeanBuildItem registerBeans()
	{
		return AdditionalBeanBuildItem.builder()
			.addBeanClass(Rage4jBeanProducer.class)
			.setUnremovable()
			.build();
	}
}
