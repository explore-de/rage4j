package dev.rage4j.asserts.openai;

import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.rage4j.asserts.RageAssert;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OpenAiLLMBuilderTest
{
	@Test
	void shouldConfigureReasoningEffortForBothChatModels() throws ReflectiveOperationException
	{
		RageAssert rageAssert = new OpenAiLLMBuilder()
			.reasoningEffort(OpenAiReasoningEffort.HIGH)
			.fromApiKey("test-key");

		OpenAiChatModel chatModel = getChatModel(rageAssert, "chatModel");
		OpenAiChatModel judgeChatModel = getChatModel(rageAssert, "judgeChatModel");

		assertEquals("high", chatModel.defaultRequestParameters().reasoningEffort());
		assertEquals("high", judgeChatModel.defaultRequestParameters().reasoningEffort());
	}

	@Test
	void shouldAllowSeparateReasoningEffortPerChatModel() throws ReflectiveOperationException
	{
		RageAssert rageAssert = new OpenAiLLMBuilder()
			.chatReasoningEffort(OpenAiReasoningEffort.LOW)
			.judgeReasoningEffort(OpenAiReasoningEffort.XHIGH)
			.fromApiKey("test-key");

		OpenAiChatModel chatModel = getChatModel(rageAssert, "chatModel");
		OpenAiChatModel judgeChatModel = getChatModel(rageAssert, "judgeChatModel");

		assertEquals("low", chatModel.defaultRequestParameters().reasoningEffort());
		assertEquals("xhigh", judgeChatModel.defaultRequestParameters().reasoningEffort());
	}

	@Test
	void shouldNotApplyChatReasoningEffortToJudgeModel() throws ReflectiveOperationException
	{
		RageAssert rageAssert = new OpenAiLLMBuilder()
			.chatReasoningEffort(OpenAiReasoningEffort.NONE)
			.fromApiKey("test-key");

		OpenAiChatModel chatModel = getChatModel(rageAssert, "chatModel");
		OpenAiChatModel judgeChatModel = getChatModel(rageAssert, "judgeChatModel");

		assertEquals("none", chatModel.defaultRequestParameters().reasoningEffort());
		assertNull(judgeChatModel.defaultRequestParameters().reasoningEffort());
	}

	@Test
	void shouldKeepReasoningEffortUnsetByDefault() throws ReflectiveOperationException
	{
		RageAssert rageAssert = new OpenAiLLMBuilder().fromApiKey("test-key");

		OpenAiChatModel chatModel = getChatModel(rageAssert, "chatModel");
		OpenAiChatModel judgeChatModel = getChatModel(rageAssert, "judgeChatModel");

		assertNull(chatModel.defaultRequestParameters().reasoningEffort());
		assertNull(judgeChatModel.defaultRequestParameters().reasoningEffort());
	}

	@Test
	void shouldSupportNoneReasoningEffort() throws ReflectiveOperationException
	{
		RageAssert rageAssert = new OpenAiLLMBuilder()
			.reasoningEffort(OpenAiReasoningEffort.NONE)
			.fromApiKey("test-key");

		OpenAiChatModel chatModel = getChatModel(rageAssert, "chatModel");
		OpenAiChatModel judgeChatModel = getChatModel(rageAssert, "judgeChatModel");

		assertEquals("none", chatModel.defaultRequestParameters().reasoningEffort());
		assertEquals("none", judgeChatModel.defaultRequestParameters().reasoningEffort());
	}

	@Test
	void shouldSupportMinimalReasoningEffort() throws ReflectiveOperationException
	{
		RageAssert rageAssert = new OpenAiLLMBuilder()
			.reasoningEffort(OpenAiReasoningEffort.MINIMAL)
			.fromApiKey("test-key");

		OpenAiChatModel chatModel = getChatModel(rageAssert, "chatModel");
		OpenAiChatModel judgeChatModel = getChatModel(rageAssert, "judgeChatModel");

		assertEquals("minimal", chatModel.defaultRequestParameters().reasoningEffort());
		assertEquals("minimal", judgeChatModel.defaultRequestParameters().reasoningEffort());
	}

	@Test
	void shouldSupportXhighReasoningEffort() throws ReflectiveOperationException
	{
		RageAssert rageAssert = new OpenAiLLMBuilder()
			.reasoningEffort(OpenAiReasoningEffort.XHIGH)
			.fromApiKey("test-key");

		OpenAiChatModel chatModel = getChatModel(rageAssert, "chatModel");
		OpenAiChatModel judgeChatModel = getChatModel(rageAssert, "judgeChatModel");

		assertEquals("xhigh", chatModel.defaultRequestParameters().reasoningEffort());
		assertEquals("xhigh", judgeChatModel.defaultRequestParameters().reasoningEffort());
	}

	@Test
	void shouldRejectUnsupportedReasoningEffort()
	{
		IllegalArgumentException exception = assertThrows(
			IllegalArgumentException.class,
			() -> new OpenAiLLMBuilder().reasoningEffort("invalid"));

		assertEquals(
			"Unsupported reasoning effort 'invalid'. Supported values: none, minimal, low, medium, high, xhigh.",
			exception.getMessage());
	}

	private static OpenAiChatModel getChatModel(RageAssert rageAssert, String fieldName) throws ReflectiveOperationException
	{
		Field field = RageAssert.class.getDeclaredField(fieldName);
		field.setAccessible(true);
		return (OpenAiChatModel) field.get(rageAssert);
	}
}
