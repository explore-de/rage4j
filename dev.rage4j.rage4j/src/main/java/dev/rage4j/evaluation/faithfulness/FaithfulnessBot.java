package dev.rage4j.evaluation.faithfulness;

import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.rage4j.evaluation.model.ArrayResponse;

import java.util.List;

public interface FaithfulnessBot
{
	@SystemMessage("You are a claims extractor llm. Your task is extraction of several self-consisted claims from a given text. Each claim must represent atomic knowledge fact. Try to extract as many claims as possible.")
	@UserMessage("Extract claims from this text: '''{{text}}'''")
	ArrayResponse extractClaims(@V("text") String text);

	@SystemMessage("""
		You are a fact checker LLM. You are provided with a context, a claim, \
		and optionally one or more images that are part of the context. \
		Determine whether the claim can be inferred from the context (and from the images, if present). \
		A claim can be inferred if it follows directly from the context OR if it can be derived through reasoning, \
		including: arithmetic calculations (e.g. summing numbers, counting items), \
		parsing structured data formats such as JSON or XML, \
		visual analysis of any provided images, \
		or logical implications of the context content. \
		Answer only true or false.""")
	@UserMessage("Can the following claim be inferred from the context? Claim: '{{claim}}' Context: '{{context}}'")
	Boolean canBeInferred(
		@UserMessage List<ImageContent> images,
		@V("claim") String claim,
		@V("context") String context);
}