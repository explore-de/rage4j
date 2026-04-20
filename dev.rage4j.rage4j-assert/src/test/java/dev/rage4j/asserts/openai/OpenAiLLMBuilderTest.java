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
	void shouldConfigureReasoningEffortForJudgeModel() throws ReflectiveOperationException
	{
		RageAssert rageAssert = new OpenAiLLMBuilder()
			.reasoningEffort(OpenAiReasoningEffort.HIGH)
			.fromApiKey("test-key");

		OpenAiChatModel judgeChatModel = getJudgeChatModel(rageAssert);

		assertEquals("high", judgeChatModel.defaultRequestParameters().reasoningEffort());
	}

	@Test
	void shouldTreatChatReasoningEffortAsJudgeConfiguration() throws ReflectiveOperationException
	{
		RageAssert rageAssert = new OpenAiLLMBuilder()
			.chatReasoningEffort(OpenAiReasoningEffort.LOW)
			.fromApiKey("test-key");

		OpenAiChatModel judgeChatModel = getJudgeChatModel(rageAssert);

		assertEquals("low", judgeChatModel.defaultRequestParameters().reasoningEffort());
	}

	@Test
	void shouldPreferExplicitJudgeReasoningEffort() throws ReflectiveOperationException
	{
		RageAssert rageAssert = new OpenAiLLMBuilder()
			.chatReasoningEffort(OpenAiReasoningEffort.LOW)
			.judgeReasoningEffort(OpenAiReasoningEffort.XHIGH)
			.fromApiKey("test-key");

		OpenAiChatModel judgeChatModel = getJudgeChatModel(rageAssert);

		assertEquals("xhigh", judgeChatModel.defaultRequestParameters().reasoningEffort());
	}

	@Test
	void shouldKeepReasoningEffortUnsetByDefault() throws ReflectiveOperationException
	{
		RageAssert rageAssert = new OpenAiLLMBuilder().fromApiKey("test-key");

		OpenAiChatModel judgeChatModel = getJudgeChatModel(rageAssert);

		assertNull(judgeChatModel.defaultRequestParameters().reasoningEffort());
	}

	@Test
	void shouldSupportNoneReasoningEffort() throws ReflectiveOperationException
	{
		RageAssert rageAssert = new OpenAiLLMBuilder()
			.reasoningEffort(OpenAiReasoningEffort.NONE)
			.fromApiKey("test-key");

		OpenAiChatModel judgeChatModel = getJudgeChatModel(rageAssert);

		assertEquals("none", judgeChatModel.defaultRequestParameters().reasoningEffort());
	}

	@Test
	void shouldSupportMinimalReasoningEffort() throws ReflectiveOperationException
	{
		RageAssert rageAssert = new OpenAiLLMBuilder()
			.reasoningEffort(OpenAiReasoningEffort.MINIMAL)
			.fromApiKey("test-key");

		OpenAiChatModel judgeChatModel = getJudgeChatModel(rageAssert);

		assertEquals("minimal", judgeChatModel.defaultRequestParameters().reasoningEffort());
	}

	@Test
	void shouldSupportXhighReasoningEffort() throws ReflectiveOperationException
	{
		RageAssert rageAssert = new OpenAiLLMBuilder()
			.reasoningEffort(OpenAiReasoningEffort.XHIGH)
			.fromApiKey("test-key");

		OpenAiChatModel judgeChatModel = getJudgeChatModel(rageAssert);

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

	private static OpenAiChatModel getJudgeChatModel(RageAssert rageAssert) throws ReflectiveOperationException
	{
		Field field = RageAssert.class.getDeclaredField("judgeChatModel");
		field.setAccessible(true);
		return (OpenAiChatModel) field.get(rageAssert);
	}
}
