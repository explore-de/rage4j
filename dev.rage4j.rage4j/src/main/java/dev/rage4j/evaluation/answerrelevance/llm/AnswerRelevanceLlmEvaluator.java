package dev.rage4j.evaluation.answerrelevance.llm;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import dev.rage4j.evaluation.Evaluation;
import dev.rage4j.evaluation.Evaluator;
import dev.rage4j.model.Sample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnswerRelevanceLlmEvaluator implements Evaluator
{

	private static final String METRIC_NAME = "Answer relevance llm";
	private static final Logger LOG = LoggerFactory.getLogger(AnswerRelevanceLlmEvaluator.class);

	private final AnswerRelevanceLlmBot bot;

	public AnswerRelevanceLlmEvaluator(ChatModel JudgeModel)
	{
		bot = AiServices.create(AnswerRelevanceLlmBot.class, JudgeModel);
	}

	/**
	 * Evaluates the given sample according to a specific metric and returns the result as an {@code Evaluation}.
	 *
	 * @param sample
	 * 	The sample containing data (such as context and answer) to be evaluated.
	 * @return An {@code Evaluation} object representing the metric name and its calculated value.
	 * @throws IllegalArgumentException
	 * 	if the sample is invalid or cannot be evaluated.
	 */
	@Override
	public Evaluation evaluate(Sample sample)
	{
		if (!sample.hasAnswer())
		{
			throw new IllegalArgumentException("Sample must have an answer for Answer Relevance LLM evaluation");
		}
		if (!sample.hasQuestion())
		{
			throw new IllegalArgumentException("Sample must have a question for Answer Relevance LLM evaluation");
		}

		String answer = sample.getAnswer();
		String question = sample.getQuestion();
		LOG.info("Evaluating new sample");
		LOG.info("Question: {}", question);
		LOG.info("Answer: {}", answer);

		String scoreGeneratedByJudge = bot.generateScore(question, answer);
		int score = Integer.parseInt(scoreGeneratedByJudge);

		return new Evaluation(METRIC_NAME, score);


	}
}
