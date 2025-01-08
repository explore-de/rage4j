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
		    You are a facts analyzer LLM. You will be provided ground truth and an generated answer. You must extract facts or statements
		    that are present in both the ground truth and the generated answer, i.e. true positive statements.
		""")
	@UserMessage(USER_MESSAGE_PROMPT)
	ArrayResponse extractTruePositiveClaims(@V("groundTruth") String groundTruth, @V("actualAnswer") String actualAnswer);

	@SystemMessage("""
		    You are a facts analyzer LLM. You will be provided ground truth and an generated answer. You must extract Facts or statements
		    that are present in the generated answer but not in the ground truth, i.e. false positive statements.
		""")
	@UserMessage(USER_MESSAGE_PROMPT)
	ArrayResponse extractFalsePositiveClaims(@V("groundTruth") String groundTruth, @V("actualAnswer") String actualAnswer);

	@SystemMessage("""
		    You are a facts analyzer LLM. You will be provided ground truth and an generated answer. You must extract facts or statements
		    that are present in the ground truth but not in the generated answer, i.e. false negative statements.
		""")
	@UserMessage(USER_MESSAGE_PROMPT)
	ArrayResponse extractFalseNegativeClaims(@V("groundTruth") String groundTruth, @V("actualAnswer") String actualAnswer);
}
