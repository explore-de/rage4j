package dev.rage4j.util;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.CosineSimilarity;

import java.util.function.BiFunction;

/**
 * The {@code StringSimilarityComputer} class computes the cosine similarity
 * between two strings. It uses an {@code EmbeddingModel} to generate embeddings
 * for the input strings and calculates the similarity using the cosine
 * similarity function.
 * <p>
 * This class implements the {@code BiFunction} interface, allowing it to be
 * used as a functional component that takes two strings and returns a
 * {@code Double} representing their similarity.
 */
public class StringSimilarityComputer implements BiFunction<String, String, Double>
{
	private final EmbeddingModel embeddingModel;

	/**
	 * Constructs a {@code StringSimilarityComputer} with the given embedding
	 * model. The embedding model is used to convert the input strings into
	 * embeddings.
	 *
	 * @param embeddingModel
	 *            The {@code EmbeddingModel} used to generate embeddings for
	 *            strings.
	 */
	public StringSimilarityComputer(EmbeddingModel embeddingModel)
	{
		this.embeddingModel = embeddingModel;
	}

	/**
	 * Applies the cosine similarity function to the embeddings of two input
	 * strings. The strings are first embedded using the {@code EmbeddingModel},
	 * and their cosine similarity is then computed.
	 *
	 * @param a
	 *            The first string to compare.
	 * @param b
	 *            The second string to compare.
	 * @return A {@code Double} representing the cosine similarity between the
	 *         embeddings of the two strings.
	 */
	@Override
	public Double apply(String a, String b)
	{
		Embedding embeddingA = embeddingModel.embed(a).content();
		Embedding embeddingB = embeddingModel.embed(b).content();
		return CosineSimilarity.between(embeddingA, embeddingB);
	}
}
