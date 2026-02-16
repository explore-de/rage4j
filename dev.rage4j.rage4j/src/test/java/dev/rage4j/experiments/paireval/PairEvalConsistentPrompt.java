package dev.rage4j.experiments.paireval;

import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * Does not work!
 */
public interface PairEvalConsistentPrompt
{
	String USER_MESSAGE_PROMPT = """
		Instruction: Your task is to evaluate the Temporal Consistency of the final response in two different dialogue scenarios. For each conversation, check if the "Response" is temporally consistent with its specific "History". Specifically, determine if the response contradicts the timeline, duration of events, or causal sequence established in the history. Compare the two outcomes and identify which response is more consistent with its context.
		
		Conversation A History:
		{{history1}}
		
		Response A:
		{{response1}}
		
		Conversation B History:
		{{history2}}
		
		Response B:
		{{response2}}
		
		Which response demonstrates better temporal consistency? Answer strictly with the label 'Response A' or 'Response B'. Do not include any reasoning, analysis, or punctuation.
		""";

	@UserMessage(USER_MESSAGE_PROMPT)
	String evaluateResponses(@V("history1") String history1, @V("response1") String response1, @V("history2") String history2, @V("response2") String response2);
}
