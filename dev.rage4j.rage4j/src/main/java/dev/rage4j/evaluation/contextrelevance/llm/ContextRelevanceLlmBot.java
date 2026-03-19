package dev.rage4j.evaluation.contextrelevance.llm;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;


public interface ContextRelevanceLlmBot
{
	@SystemMessage("""
		You are evaluating the relevance of this context chunk concerning the given question.

		chunk:
		{{chunk}}

		Question:
		{{question}}

		Score how well the chunk addresses the question.

		0 = completely irrelevant
		1 = partially relevant
		2 = mostly relevant
		3 = perfectly relevant

		Return exactly one integer: 0, 1, 2, or 3. Output must contain only that single digit and nothing else.
		""")
	@UserMessage("Evaluate the chunk.")
	String generateScore(@V("question") String question, @V("answer") String answer);
}
