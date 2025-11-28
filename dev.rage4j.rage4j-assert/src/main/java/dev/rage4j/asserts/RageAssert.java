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

	public RageAssert(ChatModel chatLanguageModel, EmbeddingModel embeddingModel)
	{
		this.chatModel = chatLanguageModel;
		this.embeddingModel = embeddingModel;
		this.observers = new ArrayList<>();
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
		return new RageAssertTestCaseBuilder(chatModel, embeddingModel, observers);
	}
}