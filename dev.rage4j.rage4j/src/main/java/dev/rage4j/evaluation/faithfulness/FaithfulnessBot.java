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

	@SystemMessage("You are a fact checker llm. Your are provided with a context and a fact. You must answer whether this fact can be inferred from the context.")
	@UserMessage("Decide whether this fact '{{claim}}' can be inferred from this context '{{info}}'")
	Boolean canBeInferred(@V("info") String info, @V("claim") String claim);
}