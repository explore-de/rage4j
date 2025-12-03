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
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Objects;

public class AxcelEvaluator implements Evaluator
{
	private static final Logger log = LoggerFactory.getLogger(AxcelEvaluator.class);
	private static final String METRIC_NAME = "Axcel factual alignment";
	private static final double MAX_RATING = 5.0;

	private final AxcelBot bot;
	private final ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(20);
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
		AxcelOneShotExamples examples = loader.loadExampleData();
		SystemMessage systemMessage = SystemMessage.from(SYSTEM_PROMPT);
		UserMessage exampleStDtPair = UserMessage.from(buildFewShotExemplars(examples.sourceText(), examples.derivedText()));
		AiMessage exampleResponseAiResponse = AiMessage.from(examples.aiResponse());

		chatMemory.clear();
		chatMemory.add(systemMessage);
		chatMemory.add(exampleStDtPair);
		chatMemory.add(exampleResponseAiResponse);

		log.info("Start Axcel evaluation...");
		String context = buildContext(sample);
		String actualStDtPair = buildFewShotExemplars(context, sample.getAnswerOrFail());
		List<AxcelFactEvaluation> parsedFacts = bot.evaluate(actualStDtPair);
		double score = normalizeScore(parsedFacts);
		logDebug(parsedFacts);
		log.info("Axcel evaluation completed. Score: {}", score);

		List<String> explanations = parsedFacts.stream().map(AxcelFactEvaluation::toString).toList();
		return new Evaluation(METRIC_NAME, score, explanations);
	}

	private static @NotNull String buildContext(Sample sample)
	{
		StringBuilder sb = new StringBuilder();
		for (String context : sample.getContextsListOrFail())
		{
			sb.append(context).append("\n");
		}
		sb.append("User: ").append(sample.getQuestionOrFail());
		return sb.toString();
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
			.filter(Objects::nonNull)
			.mapToDouble(Integer::doubleValue)
			.summaryStatistics();

		if (stats.getCount() == 0)
		{
			log.warn("Axcel response did not include any numeric ratings. Returning score 0.");
			return 0.0;
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
