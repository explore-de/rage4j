package dev.rage4j.evaluation.faithfulness;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.rage4j.evaluation.model.ArrayResponse;

public interface FaithfulnessBot
{
	@SystemMessage("You are a claims extractor llm. Your task is extraction of several self-consisted claims from a given text. Each claim must represent atomic knowledge fact. Try to extract as many claims as possible.")
	@UserMessage("Extract claims from this text: '''{{text}}'''")
	ArrayResponse extractClaims(@V("text") String text);

	@SystemMessage("""
		You are a fact checker. Your task is to determine if a claim can be inferred from a context.
		You MUST respond with ONLY the word "true" or "false", nothing else.
		Do not add explanations, reasoning, or any other text.
		""")
	@UserMessage("""
		Context: {{info}}
		Claim: {{claim}}
		
		Can the claim be inferred from the context? Answer ONLY with true or false:
		""")
	Boolean canBeInferred(@V("info") String info, @V("claim") String claim);
}