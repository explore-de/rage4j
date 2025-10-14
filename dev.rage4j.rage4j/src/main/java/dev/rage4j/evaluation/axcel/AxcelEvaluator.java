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

	private static final String SYSTEM_PROMPT = """
		You are given two texts, a source text and derived text.
		Verify if the derived text is factually correct with respect to the source.
		Use the following step-by-step instructions to assess factual correctness of derived text.
		Step 1 - Extract all the facts from the derived text.
		Step 2 - Check if the extracted facts can be verified from the source text.
		Step 3 - Rate the correctness of each fact on the scale of 1 to 5 based on the verification from previous step.
		Step 4 - Generate output in a consistent format following the format of the	examples given below.
		""";

	private static final String EXAMPLE_SOURCE_TEXT = """
		Manchester City	are keen to sign Anderlecht teenager Evangelos Patoulidis.
		The 14-year-old playmaker is regarded as one of the best talents to emerge from Anderlecht’s youth set-up and has also attracted attention from Arsenal and Barcelona.
		The Belgian starlet rejected a move to Barcelona’s La Masia academy when he was 12 as his family wanted him to continue his studies.
		He has continued to impress and City have held discussions with Anderlecht chairman Roger Vanden Stock in the hope of agreeing a compensation package.
		Manuel Pellegrini is looked to build for the future by snapping up hot property Evangelos Patoulidis.
		""";

	private static final String EXAMPLE_DERIVED_TEXT = """
		Evangelos patoulidis is regarded as one of the best players to emerge from anderlecht youth.
		He has also attracted attention from arsenal and barcelona.
		The belgian starlet rejected a move to barcelona ’s la masia academy.
		The 14-year-old has attracted interest from barcelona to barcelona.
		""";

	private static final String EXAMPLE_RESPONSE_AI_RESPONSE = """
		Let’s verify the factual accuracy of the derived text step by step:
		
		1. Evangelos Patoulidis is Regarded as One of the Best Players to Emerge from Anderlecht Youth:
		- **Derived Text:** Evangelos Patoulidis is regarded as one of the best players to emerge from Anderlecht youth.
		- **Source Text:** The source text states that Patoulidis is regarded as "one of the best talents to emerge from Anderlecht’s youth set-up".
		- **Verification:** Correct. Rating: 5
		
		2. He Has Also Attracted Attention from Arsenal and Barcelona:
		- **Derived Text:** He has also attracted attention from Arsenal and Barcelona.
		- **Source Text:** This fact is mentioned verbatim in the source text.
		- **Verification:** Correct. Rating: 5
		
		3. The Belgian Starlet Rejected a Move to Barcelona’s La Masia Academy:
		- **Derived Text:** The Belgian starlet rejected a move to Barcelona’s La Masia academy.
		- **Source Text:** The source text confirms this fact.
		- **Verification:** Correct. Rating: 5
		
		4. The 14-Year-Old Has Attracted Interest from Barcelona to Barcelona:
		- **Derived Text:** The 14-year-old has attracted interest from Barcelona to Barcelona.
		- **Source Text:** This statement is confusing and not supported by the source text.
		- **Verification:** Incorrect. Rating: 1
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
		SystemMessage systemMessage = SystemMessage.from(SYSTEM_PROMPT);
		UserMessage exampleStDtPair = UserMessage.from(buildFewShotExemplars(EXAMPLE_SOURCE_TEXT, EXAMPLE_DERIVED_TEXT));
		AiMessage exampleResponseAiResponse = AiMessage.from(EXAMPLE_RESPONSE_AI_RESPONSE);

		chatMemory.clear();
		chatMemory.add(systemMessage);
		chatMemory.add(exampleStDtPair);
		chatMemory.add(exampleResponseAiResponse);

		log.info("Start Axcel evaluation...");
		String actualStDtPair = buildFewShotExemplars(buildContext(sample), sample.getAnswerOrFail());
		List<AxcelFactEvaluation> parsedFacts = bot.evaluate(actualStDtPair);
		double score = normalizeScore(parsedFacts);
		logDebug(parsedFacts);
		log.info("Axcel evaluation completed. Score: {}", score);

		return new Evaluation(METRIC_NAME, score);
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
