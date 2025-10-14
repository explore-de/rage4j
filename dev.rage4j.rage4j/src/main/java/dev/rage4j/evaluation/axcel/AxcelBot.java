package dev.rage4j.evaluation.axcel;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.service.SystemMessage;

public interface AxcelBot
{
	@SystemMessage("""
		You are given two texts, a source text and derived text.
		Verify if the derived text is factually correct with respect to the source.
		Use the following step-by-step instructions to assess factual correctness of derived text.
		Step 1 - Extract all the facts from the derived text.
		Step 2 - Check if the extracted facts can be verified from the source text.
		Step 3 - Rate the correctness of each fact on the scale of 1 to 5 based on the verification from previous step.
		Step 4 - Generate output in a consistent format following the format of the	examples given below.
		""")
		//@UserMessage("Source Text: {{ sourceText }}")
	String evaluate(ChatMessage... chatMessages);

	// ChatResponse evaluate(ChatRequest request);
}
