package dev.rage4j.experiments.paireval;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import dev.rage4j.evaluation.paireval.PairEvalEvaluator;
import dev.rage4j.model.Sample;

class PairEvalEvaluatorConsistentPrompt extends PairEvalEvaluator
{
	private final PairEvalConsistentPrompt improvedBot;

	public PairEvalEvaluatorConsistentPrompt(ChatModel model)
	{
		super(model);
		this.improvedBot = AiServices.create(PairEvalConsistentPrompt.class, model);
	}

	@Override
	protected String getResult(Sample sampleA, Sample sampleB)
	{
		String historyA = sampleA.getContext() + sampleA.getQuestion();
		String historyB = sampleB.getContext() + sampleB.getQuestion();
		return improvedBot.evaluateResponses(
			historyA,
			sampleA.getAnswer(),
			historyB,
			sampleB.getAnswer()
		);
	}
}
