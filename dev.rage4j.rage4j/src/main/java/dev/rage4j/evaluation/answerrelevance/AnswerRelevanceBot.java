package dev.rage4j.evaluation.answerrelevance;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.rage4j.evaluation.model.ArrayResponse;

public interface AnswerRelevanceBot
{

	@SystemMessage("""
	Generate EXACTLY 5 concise questions that the answer directly answers.
	If fewer possible: Generate maximum possible (never less than 3 if content exists).
	""")
	@UserMessage("Predict 5 questions for this answer:  '{{answer}}'")
	ArrayResponse getGeneratedQuestions(@V("answer") String answer);
}
