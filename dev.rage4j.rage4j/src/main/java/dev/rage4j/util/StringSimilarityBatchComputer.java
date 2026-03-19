package dev.rage4j.util;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.CosineSimilarity;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class StringSimilarityBatchComputer implements BiFunction<String, List<String>, List<Double>>
{
	private final EmbeddingModel embeddingModel;

	/**
	 * Constructs a {@code StringSimilarityBatchComputer} with the given
	 * embedding model. The embedding model is used to convert the input strings
	 * into embeddings.
	 *
	 * @param embeddingModel
	 *            The {@code EmbeddingModel} used to generate embeddings for
	 *            strings.
	 */
	public StringSimilarityBatchComputer(EmbeddingModel embeddingModel)
	{
		this.embeddingModel = embeddingModel;
	}

	/**
	 * Computes cosine similarity scores between a single reference text and
	 * multiple target texts.
	 * <p>
	 * This method embeds the reference text once, then embeds each target text
	 * in the input list and calculates the cosine similarity between the
	 * reference embedding and each target embedding.
	 *
	 * @param a
	 *            the reference text to compare against all target texts
	 * @param b
	 *            the target texts for which similarity scores are computed
	 * @return a list of cosine similarity scores in the same order as the input
	 *         targets
	 */
	@Override
	public List<Double> apply(String a, List<String> b)
	{
		Embedding embeddingA = embeddingModel.embed(a).content();
		List<Double> result = new ArrayList<>();

		for (String instance : b)
		{
			Embedding embeddingB = embeddingModel.embed(instance).content();
			result.add(CosineSimilarity.between(embeddingA, embeddingB));
		}
		return result;
	}
}
