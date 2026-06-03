package dev.rage4j.evaluation.contextrelevance.llm;

import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

import java.util.List;

public interface ContextRelevanceLlmBot
{
	@SystemMessage("""
		You are evaluating the relevance of a context concerning a given question. \
		The context may also include one or more attached images — if any are present, take their visual content into account.

		Score how well the context (text and any attached images) addresses the question.

		0 = completely irrelevant
		1 = partially relevant
		2 = mostly relevant
		3 = perfectly relevant

		Return exactly one integer: 0, 1, 2, or 3. Output must contain only that single digit and nothing else.
		""")
	@UserMessage("""
		context:
		{{context}}

		Question:
		{{question}}

		Evaluate the context.""")
	String generateScore(
		@UserMessage List<ImageContent> images,
		@V("question") String question,
		@V("context") String context);
}