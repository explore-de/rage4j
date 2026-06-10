package dev.rage4j.asserts;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.rage4j.asserts.exception.Rage4JImplicitExplicitException;
import dev.rage4j.asserts.exception.Rage4JRefusalException;
import dev.rage4j.evaluation.bias.implicitexplicit.ImplicitExplicitEvaluator;
import dev.rage4j.evaluation.bias.implicitexplicit.support.ImplicitExplicitTemplateLibrary.GroupPair;
import dev.rage4j.model.Sample;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BiasAssertionBehaviorTest
{
	private static final GroupPair GROUP_PAIR = new GroupPair("a gay man", "a man");

	@Test
	void assertRefusalDisparityFailsInStrictModeWhenBiasExists()
	{
		RageAssertTestCaseAssertions assertions = new RageAssertTestCaseAssertions(
			refusalSample("I cannot help with that.", "Here is the answer."),
			null,
			createQueueChatModel("TRUE", "FALSE"),
			null,
			null,
			false);

		Rage4JRefusalException exception = assertThrows(Rage4JRefusalException.class, assertions::assertRefusalDisparity);

		assertEquals("Answer exceeded allowed max value! Evaluated value: 1.0, Allowed: 0.0", exception.getMessage());
	}

	@Test
	void assertImplicitExplicitFailsInStrictModeWhenBiasExists()
	{
		RageAssertTestCaseAssertions assertions = new RageAssertTestCaseAssertions(
			implicitExplicitSample("first answer", "second answer"),
			null,
			createQueueChatModel("1", "10"),
			null,
			null,
			false);

		Rage4JImplicitExplicitException exception = assertThrows(Rage4JImplicitExplicitException.class,
			() -> assertions.assertImplicitExplicit(GROUP_PAIR, ImplicitExplicitEvaluator.EXPLICIT));

		assertEquals("Answer exceeded allowed max absolute bias! Evaluated value: 1.0, Allowed absolute max: 0.0", exception.getMessage());
	}

	private static Sample refusalSample(String answer, String comparisonAnswer)
	{
		return Sample.builder()
			.withQuestion("Prompt 1")
			.withAnswer(answer)
			.withComparisonSample(Sample.builder()
				.withQuestion("Prompt 2")
				.withAnswer(comparisonAnswer)
				.build())
			.build();
	}

	private static Sample implicitExplicitSample(String answer, String comparisonAnswer)
	{
		return Sample.builder()
			.withQuestion("Prompt 1")
			.withAnswer(answer)
			.withComparisonSample(Sample.builder()
				.withQuestion("Prompt 2")
				.withAnswer(comparisonAnswer)
				.build())
			.build();
	}

	private static ChatModel createQueueChatModel(String... responses)
	{
		Queue<String> queuedResponses = new ArrayDeque<>(Arrays.asList(responses));
		return new ChatModel()
		{
			@Override
			public ChatResponse doChat(ChatRequest request)
			{
				if (queuedResponses.isEmpty())
				{
					throw new IllegalStateException("No mocked response configured for: " + request);
				}
				return ChatResponse.builder()
					.aiMessage(AiMessage.from(queuedResponses.remove()))
					.build();
			}
		};
	}
}
