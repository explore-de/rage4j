package dev.rage4j.evaluation.axcel;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import dev.rage4j.evaluation.Evaluation;
import dev.rage4j.evaluation.Evaluator;
import dev.rage4j.model.Sample;
import dev.rage4j.util.ConsistencyContextCompressorInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class AxcelEvaluator implements Evaluator
{
	private static final Logger log = LoggerFactory.getLogger(AxcelEvaluator.class);
	private static final String METRIC_NAME = "AXCEL";
	private static final double MAX_RATING = 5.0;
	private final AxcelBot bot;
	private final ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(100);
	private final AxcelDataLoader loader = new AxcelDataLoader();

	private static final String SYSTEM_PROMPT = """
		You are given two texts, a source text and derived text.
		Verify if the derived text is factually correct with respect to the source.
		Use the following step-by-step instructions to assess factual correctness of derived text.
		Step 1 - Extract all the facts from the derived text.
		Step 2 - Check if the extracted facts can be verified from the source text.
		Step 3 - Rate the correctness of each fact on the scale of 1 to 5 based on the verification from previous step.
		Step 4 - Generate output in a consistent format following the format of the	examples given below.
		""";

	private Optional<ConsistencyContextCompressorInterface> contextCompressor = Optional.empty();

	public AxcelEvaluator(ChatModel model)
	{
		this.bot = AiServices.builder(AxcelBot.class)
			.chatMemory(chatMemory)
			.chatModel(model)
			.build();
	}

	@Override
	public Evaluation evaluate(Sample sample)
	{
		AxcelOneShotExamples oneShotExample = loader.loadExampleData();
		return evaluate(sample, oneShotExample);
	}

	public Evaluation evaluate(Sample sample, AxcelOneShotExamples oneShotExample)
	{
		SystemMessage systemMessage = SystemMessage.from(SYSTEM_PROMPT);
		UserMessage exampleStDtPair = UserMessage.from(buildFewShotExemplars(oneShotExample.sourceText(), oneShotExample.derivedText()));
		AiMessage exampleResponseAiResponse = AiMessage.from(oneShotExample.aiResponse());

		chatMemory.clear();
		chatMemory.add(systemMessage);
		chatMemory.add(exampleStDtPair);
		chatMemory.add(exampleResponseAiResponse);

		log.info("Start Axcel evaluation...");
		String context = getContext(sample) + "User: " + sample.getQuestion() + "\n";
		String actualStDtPair = buildFewShotExemplars(context, sample.getAnswer());
		List<AxcelFactEvaluation> parsedFacts = bot.evaluate(actualStDtPair);
		double score = normalizeScore(parsedFacts);
		logDebug(parsedFacts);
		log.info("Axcel evaluation completed. Score: {}", score);

		List<String> explanations = parsedFacts.stream().map(AxcelFactEvaluation::toString).toList();
		return new Evaluation(METRIC_NAME, score, explanations);
	}

	public void setContextCompressor(ConsistencyContextCompressorInterface contextCompressor)
	{
		this.contextCompressor = Optional.of(contextCompressor);
	}

	private String getContext(Sample sample)
	{
		if (contextCompressor.isPresent() && sample.getContext().length() > contextCompressor.get().getTokenLimit())
		{
			log.info("Compressing context for sample with question: {}", sample.getQuestion());
			String compressedContext = contextCompressor.get().compress(sample.getContext(), sample.getQuestion());
			log.info("Context compressed from {} to {} characters", sample.getContext().length(), compressedContext.length());
			return compressedContext;
		}
		return sample.getContext();
	}

	private static void logDebug(List<AxcelFactEvaluation> parsedFacts)
	{
		if (log.isDebugEnabled())
		{
			parsedFacts.forEach(fact ->
				log.debug("Fact {} rating: {} -> {}", fact.title(), fact.rating(), fact.verification())
			);
		}
	}

	private String buildFewShotExemplars(String exampleSt, String exampleDt)
	{
		return "Source Text: "
			+ exampleSt
			+ "\n"
			+ "Derived Text: "
			+ exampleDt;
	}

	private double normalizeScore(List<AxcelFactEvaluation> facts)
	{
		DoubleSummaryStatistics stats = facts.stream()
			.map(AxcelFactEvaluation::rating)
			.filter(r -> Objects.nonNull(r) && r >= 0 && r <= MAX_RATING)
			.mapToDouble(Integer::doubleValue)
			.summaryStatistics();

		if (stats.getCount() == 0)
		{
			throw new IllegalStateException("Axcel response did not include any numeric ratings. Returning score 0.");
		}

		double averageRating = stats.getAverage();
		double normalized = averageRating / MAX_RATING;
		return clamp(normalized, 0.0, 1.0);
	}

	private double clamp(double value, double min, double max)
	{
		return Math.max(min, Math.min(max, value));
	}
}
