package dev.rage4j.util;

import dev.langchain4j.model.chat.ChatModel;

/**
 * Contract for compressing a context string based on a question while respecting a token limit.
 * Implementations are expected to preserve the most relevant information for answering the
 * question and keep the output within the configured token budget.
 *
 * @see ConsistencyContextCompressor
 */
public interface ConsistencyContextCompressorInterface
{

	/**
	 * Compresses the given context based on the question and the configured token limit.
	 *
	 * @param context the original context to compress
	 * @param question the question that guides relevance in the compressed output
	 * @return a compressed version of the context
	 */
	String compress(String context, String question);

	/**
	 * Returns the {@link ChatModel} used by this compressor.
	 *
	 * @return the ChatModel used by this compressor
	 * @see ChatModel
	 * @see #compress(String, String)
	 */
	ChatModel getChatModel();

	/**
	 * Returns the token limit that implementations should respect when producing
	 * compressed context.
	 *
	 * @return the maximum number of tokens allowed in the compressed output
	 */
	int getTokenLimit();
}
