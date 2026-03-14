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
			- Semantic match: Paraphrases count (e.g. "description of process X is Y" == "exact description for X: Y").
			- One fact per claim; maximize coverage.
			- Ignore minor wording differences if meaning identical.
			- Output ONLY JSON array: ["claim1", "claim2"]
			
			Example:
			GT: "The description of the process definition with ID 'order-process-1' is: 'Handles customer order processing workflow'."
			Answer: "The exact description of the process definition with ID 'order-process-1' is: 'Handles customer order processing workflow'."
			Output: ["The description of the process definition with ID 'order-process-1' is: 'Handles customer order processing workflow'"]
			
			Ground truth:
			{{groundTruth}}
			
			Generated answer:
			{{actualAnswer}}
			
			True Positive Claims:
			""")
	@UserMessage(USER_MESSAGE_PROMPT)
	ArrayResponse extractTruePositiveClaims(@V("groundTruth") String groundTruth, @V("actualAnswer") String actualAnswer);

	@SystemMessage("""
			    You are a facts analyzer LLM. Extract atomic claims from generated answer NOT SEMANTICALLY supported by ground truth (hallucinations/extras).
			
			    Rules:
			    - Semantic match: If paraphrase exists in GT, it's NOT FP.
			    - One fact per claim; high precision.
			    - Output ONLY JSON array: ["claim1", "claim2"]
			
			    Example:
			    GT: "Paris is in France."
			    Answer: "Paris is the capital of France and has Eiffel Tower."
			    Output: ["Paris is the capital of France"]  // If GT doesn't support "capital"
			
			    Example (no FP):
			    GT: "Paris is in France."
			    Answer: "France contains Paris."
			    Output: []  // Semantic match
			
			    Ground truth:
			    {{groundTruth}}
			
			    Generated answer:
			    {{actualAnswer}}
			
			    False Positive Claims:
			""")
	@UserMessage(USER_MESSAGE_PROMPT)
	ArrayResponse extractFalsePositiveClaims(@V("groundTruth") String groundTruth, @V("actualAnswer") String actualAnswer);

	@SystemMessage("""
                You are a facts analyzer LLM. Extract atomic claims from ground truth NOT SEMANTICALLY covered by generated answer (missing info).
            
                Rules:
                - Semantic match: If paraphrase in answer covers GT claim, it's NOT FN.
                - One fact per claim; high coverage.
                - Output ONLY JSON array: ["claim1", "claim2"]
            
                Example:
                GT: "Paris is in France and capital of France."
                Answer: "Paris is in France."
                Output: ["Paris is capital of France"]
            
                Example (no FN):
                GT: "The process handles orders."
                Answer: "Order processing is managed by the workflow."
                Output: []  // Semantic coverage
                
                Ground truth:
                {{groundTruth}}
            
                Generated answer:
                {{actualAnswer}}
            
                False Negative Claims:
            """)
	@UserMessage(USER_MESSAGE_PROMPT)
	ArrayResponse extractFalseNegativeClaims(@V("groundTruth") String groundTruth, @V("actualAnswer") String actualAnswer);
}
