package dev.rage4j.evaluation.answerrelevance;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.service.AiServices;
import dev.rage4j.evaluation.Evaluation;
import dev.rage4j.evaluation.Evaluator;
import dev.rage4j.model.Sample;
import dev.rage4j.util.StringSimilarityComputer;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.BiFunction;

/**
 * The {@code AnswerRelevanceEvaluator} class evaluates the relevance of an
 * answer by comparing the original question with generated questions derived
 * from the answer. It calculates the cosine similarity between the original
 * question and the generated questions, and returns the mean similarity as the
 * evaluation score.
 */
public class AnswerRelevanceEvaluator implements Evaluator
{
	private static final String METRIC_NAME = "Answer relevance";
	private static final Logger LOG = LoggerFactory.getLogger(AnswerRelevanceEvaluator.class);

	private final AnswerRelevanceBot bot;
	private final BiFunction<String, String, Double> stringSimilarityComputer;

	/**
	 * Constructs an {@code AnswerRelevanceEvaluator} using the provided
	 * language model and embedding model. The {@code AnswerRelevanceBot} is
	 * created using the provided {@code ChatModel}, and a string similarity
	 * computer is initialized using the {@code EmbeddingModel}.
	 *
	 * @param chatModel
	 *            The chat model used to generate the bot.
	 * @param embeddingModel
	 *            The embedding model used to compute string similarity between
	 *            questions.
	 */
	public AnswerRelevanceEvaluator(ChatModel chatModel, EmbeddingModel embeddingModel)
	{
		bot = AiServices.create(AnswerRelevanceBot.class, chatModel);
		stringSimilarityComputer = new StringSimilarityComputer(embeddingModel);
	}

	/**
	 * Constructs an {@code AnswerRelevanceEvaluator} with a provided bot and
	 * string similarity computer. This constructor is useful for testing, where
	 * the bot and similarity computer can be mocked or injected.
	 *
	 * @param bot
	 *            The {@code AnswerRelevanceBot} used to generate questions from
	 *            an answer.
	 * @param stringSimilarityComputer
	 *            A function that computes the similarity between two strings.
	 */
	public AnswerRelevanceEvaluator(AnswerRelevanceBot bot, BiFunction<String, String, Double> stringSimilarityComputer)
	{
		this.bot = bot;
		this.stringSimilarityComputer = stringSimilarityComputer;
	}

	/**
	 * Evaluates the relevance of the provided sample's answer by generating
	 * questions from the answer, comparing them to the original question, and
	 * calculating the mean cosine similarity. If no generated questions are
	 * produced, a score of 0 is returned.
	 *
	 * @param sample
	 *            The sample containing the original question and answer to be
	 *            evaluated.
	 * @return An {@code Evaluation} object containing the metric name and the
	 *         calculated relevance score.
	 */
	public Evaluation evaluate(Sample sample)
	{
		if (!sample.hasAnswer())
		{
			throw new IllegalArgumentException("Sample must have an answer for Answer Relevance evaluation");
		}
		if (!sample.hasQuestion())
		{
			throw new IllegalArgumentException("Sample must have a question for Answer Relevance evaluation");
		}

		String answer = sample.getAnswer();
		String question = sample.getQuestion();
		LOG.info("Evaluating new sample");
		LOG.info("Question: {}", question);
		LOG.info("Answer: {}", answer);

		String[] generatedQuestions = bot.getGeneratedQuestions(answer).getItems();
		if (generatedQuestions.length == 0)
		{
			LOG.info("No generated questions found.");
			return new Evaluation(METRIC_NAME, 0);
		}
		double robustCosineSimilarity = getCosineSimilarityOfRelevantQuestions(question, generatedQuestions);
		LOG.info("Answer Relevance Metric: {}", robustCosineSimilarity);
		double clipped = Math.max(0.0, Math.min(1.0, robustCosineSimilarity));
		LOG.info("Clipped Answer: {}", clipped);
		return new Evaluation(METRIC_NAME, clipped);
	}

	private double computeMedianSimilarity(String originalQuestion, String[] questions)
	{
		DescriptiveStatistics stats = new DescriptiveStatistics();

		Arrays.stream(questions)
			.map(q -> stringSimilarityComputer.apply(originalQuestion, q))
			.filter(Objects::nonNull)
			.forEach(stats::addValue);

		if (stats.getN() == 0)
		{
			return 0.0;
		}

		double median = stats.getPercentile(50.0);
		return Math.max(0.0, Math.min(1.0, median));
	}

	private double getCosineSimilarityOfRelevantQuestions(String originalQuestion, String[] generatedQuestions)
	{
		if (generatedQuestions.length == 0)
		{
			return 0.0;
		}

		double score = computeMedianSimilarity(originalQuestion, generatedQuestions);
		LOG.info("Robust Relevance score: {}", score);

		return score;
	}

}
