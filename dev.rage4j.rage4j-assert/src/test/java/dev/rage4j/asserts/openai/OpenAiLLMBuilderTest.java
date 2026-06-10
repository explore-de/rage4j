package dev.rage4j.asserts.openai;

import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.rage4j.asserts.RageAssert;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

class OpenAiLLMBuilderTest
{
	@Test
	void createsSeparateChatAndJudgeModels()
		throws Exception
	{
		RageAssert rageAssert = new OpenAiLLMBuilder()
			.chatModelName("gpt-4o-mini")
			.judgeModelName("gpt-5.1")
			.chatReasoningEffort(OpenAiReasoningEffort.LOW)
			.judgeReasoningEffort(OpenAiReasoningEffort.HIGH)
			.fromApiKey("test-key");

		OpenAiChatModel chatModel = readChatModel(rageAssert, "chatModel");
		OpenAiChatModel judgeChatModel = readChatModel(rageAssert, "judgeChatModel");

		assertNotSame(chatModel, judgeChatModel);
		assertEquals("gpt-4o-mini", chatModel.defaultRequestParameters().modelName());
		assertEquals("low", chatModel.defaultRequestParameters().reasoningEffort());
		assertEquals("gpt-5.1", judgeChatModel.defaultRequestParameters().modelName());
		assertEquals("high", judgeChatModel.defaultRequestParameters().reasoningEffort());
	}

	@Test
	void withChatModelDoesNotOverridePreviouslyConfiguredJudgeModel()
		throws Exception
	{
		RageAssert rageAssert = new OpenAiLLMBuilder()
			.judgeModelName("gpt-5.1")
			.withChatModel("gpt-4o-mini")
			.fromApiKey("test-key");

		OpenAiChatModel chatModel = readChatModel(rageAssert, "chatModel");
		OpenAiChatModel judgeChatModel = readChatModel(rageAssert, "judgeChatModel");

		assertEquals("gpt-4o-mini", chatModel.defaultRequestParameters().modelName());
		assertEquals("gpt-5.1", judgeChatModel.defaultRequestParameters().modelName());
	}

	private static OpenAiChatModel readChatModel(RageAssert rageAssert, String fieldName)
		throws ReflectiveOperationException
	{
		Field field = RageAssert.class.getDeclaredField(fieldName);
		field.setAccessible(true);
		return (OpenAiChatModel)field.get(rageAssert);
	}
}
