package dev.rage4j.experiments.context;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.rage4j.evaluation.Evaluation;
import dev.rage4j.evaluation.axcel.AxcelEvaluator;
import dev.rage4j.evaluation.axcel.AxcelOneShotExamples;
import dev.rage4j.experiments.enity.Dialog;
import dev.rage4j.experiments.enity.ExperimentEvaluation;
import dev.rage4j.util.LlmContextCompressor;
import org.jspecify.annotations.NonNull;

import static dev.langchain4j.model.chat.Capability.RESPONSE_FORMAT_JSON_SCHEMA;

public class AxcelContextCompressionTest extends AxcelContextTest
{
	private static final String OPEN_AI_MODEL_NAME = "gpt-5.2";
	private static final String OPEN_AI_KEY = System.getenv("OPEN_AI_KEY");

	private static final LlmContextCompressor COMPRESSOR = new LlmContextCompressor(getOpenAIChatModel(), 512);

	@Override
	protected @NonNull ExperimentEvaluation getExperimentEvaluation(int context, Dialog dialog, AxcelOneShotExamples oneShotExample)
	{
		// given
		AxcelEvaluator evaluator = new AxcelEvaluator(getOllamaChatModel(context));
		evaluator.setContextCompressor(COMPRESSOR);

		// when
		Evaluation evaluation = evaluator.evaluate(dialog.getSample(), oneShotExample);
		return new ExperimentEvaluation(evaluation, dialog.path());
	}

	private static ChatModel getOpenAIChatModel()
	{
		return OpenAiChatModel.builder()
			.apiKey(OPEN_AI_KEY)
			.modelName(OPEN_AI_MODEL_NAME)
			.supportedCapabilities(RESPONSE_FORMAT_JSON_SCHEMA)
			.strictJsonSchema(true)
			.temperature(0.5)
			.build();
	}
}
