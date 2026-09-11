package dev.rage4j.evaluation.answerrelevance.llm;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.rage4j.evaluation.model.ScoreWithReasonResponse;

/**
 * Judge prompts for the answer relevance metric. The question and the answer
 * are passed in the user message, never in the system message: judge models
 * that treat the system message strictly as instructions (e.g. gpt-oss, whose
 * chat format maps it to a "developer" role) look for the answer in the user
 * turn, and when it is not there they score 0 with a reason such as "No answer
 * content was provided" — a verdict indistinguishable from a real 0. The other
 * judge bots in this module already use that layout.
 */
public interface AnswerRelevanceLlmBot
{
	@SystemMessage("""
		You are evaluating the relevance of an answer concerning a given question.

		Score how well the answer addresses the question.

		0 = completely irrelevant
		1 = partially relevant
		2 = mostly relevant
		3 = perfectly relevant

		Also provide a short reason for the score in one sentence.
		""")
	@UserMessage("""
		Question:
		{{question}}

		Answer:
		{{answer}}

		Evaluate the answer.""")
	ScoreWithReasonResponse generateScoreWithReason(@V("question") String question, @V("answer") String answer);

	@SystemMessage("""
		You are evaluating the relevance of an answer concerning a given question.

		Score how well the answer addresses the question.

		0 = completely irrelevant
		1 = partially relevant
		2 = mostly relevant
		3 = perfectly relevant

		Return exactly one integer: 0, 1, 2, or 3. Output must contain only that single digit and nothing else.
		""")
	@UserMessage("""
		Question:
		{{question}}

		Answer:
		{{answer}}

		Evaluate the answer.""")
	String generateScore(@V("question") String question, @V("answer") String answer);
}
