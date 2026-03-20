package dev.rage4j.evaluation.answerrelevance.llm;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.rage4j.evaluation.model.ScoreWithReasonResponse;

public interface AnswerRelevanceLlmBot
{
	@SystemMessage("""
		You are evaluating the relevance of an answer.

		Question:
		{{question}}

		Answer:
		{{answer}}

		Score how well the answer addresses the question.

		0 = completely irrelevant
		1 = partially relevant
		2 = mostly relevant
		3 = perfectly relevant

		Also provide a short reason for the score in one sentence.
		""")
	@UserMessage("Evaluate this answer.")
	ScoreWithReasonResponse generateScoreWithReason(@V("question") String question, @V("answer") String answer);

	@SystemMessage("""
		You are evaluating the relevance of an answer.

		Question:
		{{question}}

		Answer:
		{{answer}}

		Score how well the answer addresses the question.

		0 = completely irrelevant
		1 = partially relevant
		2 = mostly relevant
		3 = perfectly relevant

		Return exactly one integer: 0, 1, 2, or 3. Output must contain only that single digit and nothing else.
		""")
	@UserMessage("Evaluate this answer.")
	String generateScore(@V("question") String question, @V("answer") String answer);

}
