package dev.rage4j.evaluation.paireval;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import dev.rage4j.evaluation.Evaluation;
import dev.rage4j.evaluation.Evaluator;
import dev.rage4j.model.Sample;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class PairEvalEvaluator implements Evaluator
{
	public static final String NAME = "Pair evaluation";
	private static final Logger log = LoggerFactory.getLogger(PairEvalEvaluator.class);
	private final PairEvalBot bot;
	// The sample will be evaluated against the few shots examples
	private final List<Sample> fewShotExamples;

	public PairEvalEvaluator(ChatModel model)
	{
		bot = AiServices.create(PairEvalBot.class, model);
		fewShotExamples = new PairEvalExampleLoader().loadExampleData();
	}

	@Override
	public Evaluation evaluate(Sample sample)
	{
		int isBetterCount = 0;
		for (Sample example : fewShotExamples)
		{
			String result = getResult(sample, example);
			if (result.equals("Response A"))
			{
				isBetterCount++;
			}
			result = getResult(example, sample);
			if (result.equals("Response B"))
			{
				isBetterCount++;
			}
		}

		double value = ((double)isBetterCount / (fewShotExamples.size() * 2));
		log.info("PairEvalEvaluator result for sample '{}': {}", sample.getQuestionOrFail(), value);
		return new Evaluation(NAME, value);
	}

	private String getResult(Sample sampleA, Sample sampleB)
	{
		String historyA = buildContext(sampleA);
		String historyB = buildContext(sampleB);
		return bot.evaluateResponses(
			historyA,
			sampleA.getAnswerOrFail(),
			historyB,
			sampleB.getAnswerOrFail()
		);
	}

	private static @NotNull String buildContext(Sample sample)
	{
		StringBuilder sb = new StringBuilder();
		for (String context : sample.getContextsListOrFail())
		{
			sb.append(context).append("\n");
		}
		if (sample.hasQuestion())
		{
			sb.append(sample.getQuestionOrFail());
		}
		return sb.toString();
	}
}
