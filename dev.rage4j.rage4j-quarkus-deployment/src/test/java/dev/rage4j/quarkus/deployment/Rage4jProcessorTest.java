package dev.rage4j.quarkus.deployment;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.nio.file.Path;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import dev.langchain4j.model.chat.ChatModel;
import dev.rage4j.asserts.RageAssert;
import dev.rage4j.persist.EvaluationStore;
import dev.rage4j.persist.store.JsonLinesStore;
import io.quarkiverse.langchain4j.ModelName;
import io.quarkus.arc.ClientProxy;
import io.quarkus.test.QuarkusExtensionTest;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

public class Rage4jProcessorTest
{
	@RegisterExtension
	static final QuarkusExtensionTest config = new QuarkusExtensionTest()
		.setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
			.addClasses(TestModels.class, ModelName.class)
			.addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
			.addAsResource(new StringAsset("quarkus.rage4j.evaluation-mode=true\n"
				+ "quarkus.rage4j.chat-model-bean=model-one\n"
				+ "quarkus.rage4j.persistence.enabled=true\n"
				+ "quarkus.rage4j.persistence.file=target/rage4j-extension-test.jsonl\n"), "application.properties"));

	@Inject
	RageAssert rageAssert;

	@Inject
	EvaluationStore evaluationStore;

	@Test
	void resolvesNamedModelAndConfiguresBeans() throws ReflectiveOperationException
	{
		assertNotNull(rageAssert);
		Object rageAssertInstance = ClientProxy.unwrap(rageAssert);
		Field evaluationMode = RageAssert.class.getDeclaredField("evaluationMode");
		evaluationMode.setAccessible(true);
		assertTrue(evaluationMode.getBoolean(rageAssertInstance));

		JsonLinesStore store = assertInstanceOf(JsonLinesStore.class, ClientProxy.unwrap(evaluationStore));
		assertEquals(Path.of("target/rage4j-extension-test.jsonl"), store.getFile());
	}

	@ApplicationScoped
	public static class TestModels
	{
		@Produces
		@ApplicationScoped
		@ModelName("model-one")
		ChatModel chatModel()
		{
			return new TestChatModel();
		}

		@Produces
		@ApplicationScoped
		@ModelName("model-two")
		ChatModel secondChatModel()
		{
			return new TestChatModel();
		}
	}

	static class TestChatModel implements ChatModel
	{
	}

}
