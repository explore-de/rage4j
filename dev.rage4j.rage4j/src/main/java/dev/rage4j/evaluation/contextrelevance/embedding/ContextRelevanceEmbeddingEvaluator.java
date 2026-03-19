package dev.rage4j.evaluation.contextrelevance.embedding;

import dev.rage4j.evaluation.Evaluation;
import dev.rage4j.evaluation.Evaluator;
import dev.rage4j.evaluation.answerrelevance.embedding.AnswerRelevanceEmbeddingEvaluator;
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
	private final BiFunction<String, String, Double> stringSimilarityComputer;

	public ContextRelevanceEmbeddingEvaluator(BiFunction<String, String, Double> stringSimilarityComputer)
	{
		this.stringSimilarityComputer = stringSimilarityComputer;
	}

	@Override
	public Evaluation evaluate(Sample sample)
	{

		if (!sample.hasQuestion())
		{
			throw new IllegalArgumentException("Sample must have a question for Answer Relevance embedding evaluation");
		}

		if (!sample.hasContext())
		{
			throw new IllegalArgumentException("Sample must have context for Context Relevance embedding evaluation");
		}

		String question = sample.getQuestion();
		String context = sample.getContext();
		LOG.info("Evaluating new sample");

		List<String> chunks = ContextChunker.chunk(context);
		if (chunks.isEmpty())
		{
			return new Evaluation(METRIC_NAME, 0.0);
		}

		double best = 0.0;
		for (String chunk : chunks)
		{
			double s = stringSimilarityComputer.apply(question, chunk);
			best = Math.max(best, s);
		}

		LOG.info("Context relevance best similarity: {}", best);
		return new Evaluation(METRIC_NAME, best);
	}

}
