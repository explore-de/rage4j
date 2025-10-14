package dev.rage4j.evaluation.axcel;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
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
	private static final String EXAMPLE_RESPONSE_PATH = "exampleResponse";
	private static final AxcelResponseParser RESPONSE_PARSER = new AxcelResponseParser();
	private final AxcelBot bot;

	private static final String EXAMPLE_SOURCE_TEXT = """
		User: Hello, my name is John.
		AI: Hello John, how can I assist you today?
		User: Can you tell me a joke?
		AI: Sure! Why don't scientists trust atoms? Because they make up everything!
		User: Good one! Do you know any facts about space?
		""";

	private static final String EXAMPLE_DERIVED_TEXT = """
		Sure thing John! Here are some facts about space...
		""";

	private static final String EXAMPLE_RESPONSE_AI_RESPONSE = """
		Let's verify the factual accuracy of the derived text step by step:
		
		1. John is interacting with an AI:
		- **Derived Text:** Sure thing John! Here are some facts about space..."
		- **Source Text:** User: Hello, my name is John. AI: Hello John, how can I assist you today?
		- **Verification:** Correct. Rating: 5
		
		2. The AI is providing facts about space to John:
		- **Derived Text:** Sure thing John! Here are some facts about space..."
		- **Source Text:** User: Good one! Do you know any facts about space?
		- **Verification:** The derived text makes a claim about providing facts, but the source text does not provide any actual facts about space. Therefore, this cannot be verified. Rating: 1
		
		3. The AI told a joke to John:
		- **Derived Text:** Sure thing John! Here are some facts about space..."
		- **Source Text:** User: Can you tell me a joke? AI: Sure! Why don't scientists trust atoms? Because they make up everything!
		- **Verification:** The derived text does not mention this fact. Rating: N/A
		
		Overall, we can conclude that some of the facts presented in the derived text are aligned with the source. For the fact that cannot be verified, we are assuming that the factual information isn't provided in the source text.
		""";

	public AxcelEvaluator(ChatModel model)
	{
		this.bot = AiServices.create(AxcelBot.class, model);
	}

	@Override
	public Evaluation evaluate(Sample sample)
	{
		ChatMessage exampleUserMsg = UserMessage.from(buildFewShotExemplars(EXAMPLE_SOURCE_TEXT, EXAMPLE_DERIVED_TEXT));
		ChatMessage exampleResponseAiResponse = AiMessage.from(EXAMPLE_RESPONSE_AI_RESPONSE);
		String contexts = buildContextFromHistory(sample);
		ChatMessage actualUserMsg = UserMessage.from(buildFewShotExemplars(contexts, sample.getAnswerOrFail()));

		log.info("Start Axcel evaluation...");
		String evaluation = bot.evaluate(exampleUserMsg, exampleResponseAiResponse, actualUserMsg);
		List<AxcelFactEvaluation> parsedFacts = RESPONSE_PARSER.parse(evaluation);
		double score = normalizeScore(parsedFacts);
		logDebug(parsedFacts);
		log.info("Axcel evaluation completed. Score: {}", score);

		return new Evaluation(METRIC_NAME, score);
	}

	private static @NotNull String buildContextFromHistory(Sample sample)
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
		StringBuilder sb = new StringBuilder();
		sb.append("Source Text: ");
		sb.append(exampleSt);
		sb.append("\n");
		sb.append("Derived Text: ");
		sb.append(exampleDt);
		return sb.toString();
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
