package dev.rage4j.evaluation.answersemanticsimilarity;

import dev.rage4j.evaluation.Evaluation;
import dev.rage4j.evaluation.Evaluator;
import dev.rage4j.model.Sample;
import dev.rage4j.util.StringSimilarityComputer;
import dev.langchain4j.model.embedding.EmbeddingModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BiFunction;

/**
 * The {@code AnswerSemanticSimilarityEvaluator} class evaluates the semantic
 * similarity between the provided answer and the ground truth. It uses a string
 * similarity function, typically based on cosine similarity between embeddings,
 * to compute the relevance of the answer to the ground truth.
 * <p>
 * The result of the evaluation is returned as a score between 0 and 1,
 * representing the degree of semantic similarity.
 */
public class AnswerSemanticSimilarityEvaluator implements Evaluator
{
	private static final String METRIC_NAME = "Answer semantic similarity";
	private static final Logger LOG = LoggerFactory.getLogger(AnswerSemanticSimilarityEvaluator.class);
	private final BiFunction<String, String, Double> stringSimilarityComputer;

	/**
	 * Constructs an {@code AnswerSemanticSimilarityEvaluator} using the
	 * provided embedding model. The embedding model is used to compute the
	 * semantic similarity between the answer and the ground truth.
	 *
	 * @param embeddingModel
	 *            The embedding model used to compute the similarity between the
	 *            strings.
	 */
	public AnswerSemanticSimilarityEvaluator(EmbeddingModel embeddingModel)
	{
		this.stringSimilarityComputer = new StringSimilarityComputer(embeddingModel);
	}

	/**
	 * Constructs an {@code AnswerSemanticSimilarityEvaluator} with a custom
	 * string similarity function. This constructor allows for injecting a
	 * custom similarity function for testing or other use cases.
	 *
	 * @param stringSimilarityComputer
	 *            A function that computes the similarity between two strings.
	 */
	public AnswerSemanticSimilarityEvaluator(BiFunction<String, String, Double> stringSimilarityComputer)
	{
		this.stringSimilarityComputer = stringSimilarityComputer;
	}

	/**
	 * Evaluates the semantic similarity between the provided answer and ground
	 * truth in the {@code Sample}. The similarity is calculated using the
	 * string similarity function.
	 *
	 * @param sample
	 *            The sample containing the answer and ground truth for
	 *            evaluation.
	 * @return An {@code Evaluation} object containing the similarity score and
	 *         the metric name.
	 * @throws IllegalStateException
	 *             if either the answer or ground truth is missing in the
	 *             sample.
	 */
	@Override
	public Evaluation evaluate(Sample sample)
	{
		String answer = sample.getAnswerOrFail();
		String groundTruth = sample.getGroundTruthOrFail();
		LOG.info("Evaluating new sample");
		LOG.info("Ground truth: {}", groundTruth);
		LOG.info("Answer: {}", answer);

		double semanticSimilarity = stringSimilarityComputer.apply(answer, groundTruth);
		LOG.info("Semantic similarity: {}", semanticSimilarity);
		return new Evaluation(METRIC_NAME, semanticSimilarity);
	}
}
