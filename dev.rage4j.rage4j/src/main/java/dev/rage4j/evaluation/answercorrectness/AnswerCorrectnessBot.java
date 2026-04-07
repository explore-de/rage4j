package dev.rage4j.evaluation.answercorrectness;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.rage4j.evaluation.model.ArrayResponse;

public interface AnswerCorrectnessBot
{
	String USER_MESSAGE_PROMPT = """
		    Extract claims from this ground truth and generated answer.

		    Ground truth:
		    "{{groundTruth}}"

		    Generated answer:
		    "{{actualAnswer}}"
		""";


	@SystemMessage("""
	You are a facts analyzer LLM. Extract atomic claims (single facts) SEMANTICALLY present in BOTH ground truth and generated answer.
	
	Rules:
	- Semantic match: Paraphrases count (e.g. "description of the book X is Y" == "exact description for X: Y").
	- One fact per claim; maximize coverage.
	- Ignore minor wording differences if meaning identical.
	- Always fully write the claim you extracted in the output
	
	Example:
	GT: "The description of the book with ID 'book-42' is: 'A historical novel about the rise and fall of an ancient empire'."
	Answer: "The exact description of the book with ID 'book-42' is: 'A historical novel about the rise and fall of an ancient empire'."
	Output: ["The description of the book with ID 'book-42' is: 'A historical novel about the rise and fall of an ancient empire'"]
	""")
	@UserMessage(USER_MESSAGE_PROMPT)
	ArrayResponse extractTruePositiveClaims(@V("groundTruth") String groundTruth, @V("actualAnswer") String actualAnswer);

	@SystemMessage("""
	You are a facts analyzer LLM. Extract atomic claims from generated answer NOT SEMANTICALLY supported by ground truth (hallucinations/extras).

	Rules:
	- Semantic match: If paraphrase exists in GT, it's NOT FP.
	- One fact per claim; high precision.
	- Always fully write the claim you extracted in the output

	Example:
	GT: "Paris is in France."
	Answer: "Paris is the capital of France and has Eiffel Tower."
	Output: ["Paris is the capital of France"]  // If GT doesn't support "capital"

	Example (no FP):
	GT: "Paris is in France."
	Answer: "France contains Paris."
	Output: []  // Semantic match
	""")
	@UserMessage(USER_MESSAGE_PROMPT)
	ArrayResponse extractFalsePositiveClaims(@V("groundTruth") String groundTruth, @V("actualAnswer") String actualAnswer);

	@SystemMessage("""
	You are a facts analyzer LLM. Extract atomic claims from ground truth NOT SEMANTICALLY covered by generated answer (missing info).
	Rules:
	- Semantic match: If paraphrase in answer covers GT claim, it's NOT FN.
	- One fact per claim; high coverage.
	- Always fully write the claim you extracted in the output (Do not write something like this: [ "Paris is the la..." ] instead write the full claim "Paris is the largest city of France")

	Example:
	GT: "Paris is in France and capital of France."
	Answer: "Paris is in France."
	Output: ["Paris is capital of France"]

	Example (no FN):
	GT: "The process handles orders."
	Answer: "Order processing is managed by the workflow."
	Output: []  // Semantic coverage
	""")
	@UserMessage(USER_MESSAGE_PROMPT)
	ArrayResponse extractFalseNegativeClaims(@V("groundTruth") String groundTruth, @V("actualAnswer") String actualAnswer);
}
