package dev.rage4j.evaluation.paireval;

import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface PairEvalBot
{
	// ToDo: Maybe instead of "better" use more specific criteria like "consistent"
	String USER_MESSAGE_PROMPT = """
		Instruction: Given two conversations and responses, which response is better?
		
		Conversation A:
		{{history1}}
		
		Response A:
		{{response1}}
		
		Conversation B:
		{{history2}}
		
		Response B:
		{{response2}}
		
		Which response is better? Just answer in 'Response A' or 'Response B'.
		""";

	@UserMessage(USER_MESSAGE_PROMPT)
	String evaluateResponses(@V("history1") String history1, @V("response1") String response1, @V("history2") String history2, @V("response2") String response2);
}
