package dev.rage4j.experiments.context;

import dev.rage4j.evaluation.Evaluation;
import dev.rage4j.evaluation.paireval.PairEvalEvaluator;
import dev.rage4j.experiments.enity.Dialog;
import dev.rage4j.experiments.enity.ExperimentEvaluation;
import dev.rage4j.util.LlmContextCompressor;
import org.jspecify.annotations.NonNull;

import static dev.rage4j.experiments.context.AxcelContextCompressionTest.getOpenAIChatModel;

public class PairEvalContextCompressionTest extends PairEvalContextTest
{
	private static final int TOKEN_LIMIT = 512;

	private static final LlmContextCompressor COMPRESSOR = new LlmContextCompressor(getOpenAIChatModel(), TOKEN_LIMIT);

	@Override
	protected @NonNull ExperimentEvaluation getExperimentEvaluation(String model, int context, Dialog dialog)
	{
		// given
		PairEvalEvaluator evaluator = new PairEvalEvaluator(getOllamaChatModel(model, context));
		evaluator.enableSimpleContextCompression(TOKEN_LIMIT);

		// when
		Evaluation evaluation = evaluator.evaluate(dialog.getSample());
		return new ExperimentEvaluation(evaluation, dialog.path());
	}
}