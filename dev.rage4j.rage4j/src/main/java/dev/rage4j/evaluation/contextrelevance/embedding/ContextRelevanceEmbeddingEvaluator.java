package dev.rage4j.evaluation.contextrelevance.embedding;

import dev.rage4j.evaluation.Evaluation;
import dev.rage4j.evaluation.Evaluator;
import dev.rage4j.model.Sample;
import dev.rage4j.util.ContextChunker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.BiFunction;

public class ContextRelevanceEmbeddingEvaluator implements Evaluator
{

	private static final String METRIC_NAME = "context relevance embedding";
	private static final Logger LOG = LoggerFactory.getLogger(ContextRelevanceEmbeddingEvaluator.class);
	private final BiFunction<String, List<String>, List<Double>> stringSimilarityBatchComputer;

	public ContextRelevanceEmbeddingEvaluator(BiFunction<String, List<String>, List<Double>> stringSimilarityBatchComputer)
	{
		this.stringSimilarityBatchComputer = stringSimilarityBatchComputer;

	}

	@Override
	public Evaluation evaluate(Sample sample)
	{

		if (!sample.hasQuestion())
		{
			throw new IllegalArgumentException("Sample must have a question for context relevance embedding evaluation");
		}

		if (!sample.hasContext())
		{
			throw new IllegalArgumentException("Sample must have context for context relevance embedding evaluation");
		}

		String question = sample.getQuestion();
		String context = sample.getContext();
		LOG.info("Evaluating new sample");
		LOG.info("Question: {}", question);
		LOG.info("Context: {}", context);

		List<String> chunks = ContextChunker.chunk(context);
		if (chunks.isEmpty())
		{
			return new Evaluation(METRIC_NAME, 0.0);
		}

		var similarityScores = stringSimilarityBatchComputer.apply(question, chunks);

		double sumSimilarity = similarityScores.stream().mapToDouble(Double::doubleValue).sum();
		double result = sumSimilarity / similarityScores.size();

		LOG.info("Context relevance average similarity: {}", result);
		return new Evaluation(METRIC_NAME, result);
	}

}
