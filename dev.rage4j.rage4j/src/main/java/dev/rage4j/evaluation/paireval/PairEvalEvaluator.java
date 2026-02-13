package dev.rage4j.evaluation.paireval;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import dev.rage4j.evaluation.Evaluation;
import dev.rage4j.evaluation.Evaluator;
import dev.rage4j.model.Sample;
import dev.rage4j.util.ContextCompressor;
import dev.rage4j.util.LlmContextCompressor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class PairEvalEvaluator implements Evaluator
{
	public static final String NAME = "Pair evaluation";
	private static final Logger log = LoggerFactory.getLogger(PairEvalEvaluator.class);
	private final PairEvalBot bot;
	// The sample will be evaluated against the few shots examples
	private final List<Sample> fewShotExamples;

	private final ChatModel model;
	private Optional<ContextCompressor> contextCompressor = Optional.empty();

	public PairEvalEvaluator(ChatModel model)
	{
		this.model = model;
		bot = AiServices.create(PairEvalBot.class, model);
		fewShotExamples = new PairEvalExampleLoader().loadExampleData();
	}

	@Override
	public Evaluation evaluate(Sample sample)
	{
		if (contextCompressor.isPresent() && sample.getContext().length() > contextCompressor.get().getTokenLimit())
		{
			sample = getCompressedSample(sample);
		}
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
		log.info("PairEvalEvaluator result for sample '{}': {}", sample.getQuestion(), value);
		return new Evaluation(NAME, value);
	}

	public void enableSimpleContextCompression(int tokenLimit)
	{
		this.contextCompressor = Optional.of(new LlmContextCompressor(model, tokenLimit));
	}

	public void setContextCompressor(ContextCompressor contextCompressor)
	{
		this.contextCompressor = Optional.of(contextCompressor);
	}

	private Sample getCompressedSample(Sample sample)
	{
		if (contextCompressor.isEmpty())
		{
			log.warn("Context compressor is not set, cannot compress the context.");
			return sample;
		}
		log.info("Compressing context for sample with question: {}", sample.getQuestion());
		String compressedContext = contextCompressor.get().compress(sample.getContext(), sample.getQuestion());
		log.info("Context compressed from {} to {} characters", sample.getContext().length(), compressedContext.length());
		return new Sample.SampleBuilder()
			.withContext(compressedContext)
			.withQuestion(sample.getQuestion())
			.withAnswer(sample.getAnswer())
			.build();
	}

	private String getResult(Sample sampleA, Sample sampleB)
	{
		String historyA = sampleA.getContext() + sampleA.getQuestion();
		String historyB = sampleB.getContext() + sampleB.getQuestion();
		return bot.evaluateResponses(
			historyA,
			sampleA.getAnswer(),
			historyB,
			sampleB.getAnswer()
		);
	}
}
