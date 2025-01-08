package dev.rage4j.evaluation.answerrelevance;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.rage4j.evaluation.model.ArrayResponse;

public interface AnswerRelevanceBot
{

	@SystemMessage("You are a question predictor LLM. You task is the prediction of 5 short questions that can be answered by the sentence provided by user. If the answer is empty, return an empty Array `[]`.")
	@UserMessage("Predict 5 questions for this answer:  '{{answer}}'")
	ArrayResponse getGeneratedQuestions(@V("answer") String answer);
}
