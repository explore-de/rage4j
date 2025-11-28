package dev.rage4j.asserts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;

public class RageAssert
{
	private final ChatModel chatModel;
	private final EmbeddingModel embeddingModel;
	private final List<AssertionObserver> observers;
	private boolean evaluationMode = false;

	public RageAssert(ChatModel chatLanguageModel, EmbeddingModel embeddingModel)
	{
		this.chatModel = chatLanguageModel;
		this.embeddingModel = embeddingModel;
		this.observers = new ArrayList<>();
	}

	/**
	 * Enables evaluation mode where assertion failures log warnings instead of
	 * throwing exceptions. This allows complete evaluation runs for data
	 * collection purposes without stopping on failures.
	 *
	 * @return This RageAssert instance for method chaining.
	 */
	public RageAssert withEvaluationMode()
	{
		this.evaluationMode = true;
		return this;
	}

	/**
	 * Disables evaluation mode (strict assertions). This is the default
	 * behavior where assertion failures throw exceptions.
	 *
	 * @return This RageAssert instance for method chaining.
	 */
	public RageAssert withStrictMode()
	{
		this.evaluationMode = false;
		return this;
	}

	/**
	 * Returns whether evaluation mode is enabled.
	 *
	 * @return true if evaluation mode is enabled, false otherwise.
	 */
	boolean isEvaluationMode()
	{
		return evaluationMode;
	}

	/**
	 * Adds an observer that will be notified after each assertion evaluation.
	 *
	 * @param observer
	 *            The observer to add.
	 * @return This RageAssert instance for method chaining.
	 */
	public RageAssert addObserver(AssertionObserver observer)
	{
		this.observers.add(observer);
		return this;
	}

	/**
	 * Returns an unmodifiable list of registered observers.
	 *
	 * @return The list of observers.
	 */
	List<AssertionObserver> getObservers()
	{
		return Collections.unmodifiableList(observers);
	}

	public RageAssertTestCaseBuilder given()
	{
		return new RageAssertTestCaseBuilder(chatModel, embeddingModel, observers, evaluationMode);
	}
}