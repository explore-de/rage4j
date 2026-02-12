package dev.rage4j.util;

import dev.langchain4j.model.chat.ChatModel;

import java.util.HashMap;
import java.util.Map;

public class LlmContextCompressor implements ContextCompressor
{
	private final ChatModel chatModel;
	private final int tokenLimit;

	private static final String PROMPT = """
		You are compressing a conversation between two speakers: User and AI.
		
		GOAL
		- Produce a shorter representation that preserves:
		  - The essential facts, decisions, and open questions revenant for the last message from the user.
		  - The overall flow of the conversation
		  - Which speaker contributed which information
		
		CONSTRAINTS
		- STRICTLY preserve speaker attribution.
		- ALWAYS prefix each compressed utterance with "User:" or "AI:".
		- Do NOT invent any information or motivations that are not explicitly present.
		- Maximum length: %d tokens/words (hard budget).
		- If you must drop something, drop small talk, repetitions, and low‑impact details first.
		
		FORMAT
		Return a list of compressed turns like this:
		
		User: <compressed content of what the user contributed in this segment>
		AI: <compressed content of the assistant's response>
		...
		
		If multiple original turns in a row can be merged into one compressed turn from the same speaker, you may merge them, but keep ordering.
		
		LAST MASSAGE:
		
		%s
		
		Now compress the following conversation:
		
		%s
		""";

	private Map<String, String> contextCache = new HashMap<>();

	public LlmContextCompressor(ChatModel chatModel, int tokenLimit)
	{
		this.chatModel = chatModel;
		this.tokenLimit = tokenLimit;
	}

	@Override
	public String compress(String context, String question)
	{
		if (contextCache.containsKey(context))
		{
			return contextCache.get(context);
		}
		String formattedPrompt = String.format(PROMPT, tokenLimit, question, context);
		String compressedContext = getChatModel().chat(formattedPrompt);
		contextCache.put(context, compressedContext);
		return compressedContext;
	}

	@Override
	public ChatModel getChatModel()
	{
		return this.chatModel;
	}

	@Override
	public int getTokenLimit()
	{
		return this.tokenLimit;
	}
}
