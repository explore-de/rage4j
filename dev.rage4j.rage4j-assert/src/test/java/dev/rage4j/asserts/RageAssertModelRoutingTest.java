package dev.rage4j.asserts;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RageAssertModelRoutingTest
{
	@Test
	void assertionsUseJudgeModelInsteadOfEvaluatedModel()
	{
		ScriptedChatModel judgeModel = new ScriptedChatModel("{\"items\":[]}");
		RageAssert rageAssert = new RageAssert(new FailingChatModel(), judgeModel);

		rageAssert.given()
			.question("What is the capital of France?")
			.groundTruth("Paris is the capital of France.")
			.when()
			.answer("Paris is the capital of France.")
			.then()
			.assertAnswerCorrectness(0.0);

		assertEquals(3, judgeModel.calls);
	}

	private static final class ScriptedChatModel implements ChatModel
	{
		private final String response;
		private int calls;

		private ScriptedChatModel(String response)
		{
			this.response = response;
		}

		@Override
		public ChatResponse doChat(ChatRequest request)
		{
			calls++;
			return ChatResponse.builder()
				.aiMessage(AiMessage.from(response))
				.build();
		}
	}

	private static final class FailingChatModel implements ChatModel
	{
		@Override
		public ChatResponse doChat(ChatRequest request)
		{
			throw new AssertionError("The evaluated model must not be used as the assertion judge.");
		}

		@Override
		public String chat(String userMessage)
		{
			throw new AssertionError("The evaluated model must not be used as the assertion judge.");
		}
	}
}
