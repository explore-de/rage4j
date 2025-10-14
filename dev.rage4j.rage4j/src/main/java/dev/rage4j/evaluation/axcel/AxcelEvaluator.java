package dev.rage4j.evaluation.axcel;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import dev.rage4j.evaluation.Evaluation;
import dev.rage4j.evaluation.Evaluator;
import dev.rage4j.model.Sample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AxcelEvaluator implements Evaluator
{
	private static final Logger log = LoggerFactory.getLogger(AxcelEvaluator.class);
	private final AxcelBot bot;

	private static final String exampleSt = """
		Manchester City
		are keen to sign Anderlecht teenager
		Evangelos Patoulidis. The 14-year-old
		playmaker is regarded as one of the best
		talents to emerge from Anderlecht’s youth
		set-up and has also attracted attention
		from Arsenal and Barcelona. The Belgian
		starlet rejected a move to Barcelona’s
		La Masia academy when he was 12 as
		his family wanted him to continue his
		studies . He has continued to impress
		and City have held discussions with
		Anderlecht chairman Roger Vanden Stock
		in the hope of agreeing a compensation
		package. Manuel Pellegrini is looked to
		build for the future by snapping up hot
		property Evangelos Patoulidis.
		""";

	private static final String exampleDt = """
		Evangelos patoulidis is regarded as one of
		the best players to emerge from anderlecht
		youth. He has also attracted attention
		from arsenal and barcelona. The belgian
		starlet rejected a move to barcelona
		’s la masia academy. The 14-year-old
		has attracted interest from barcelona to
		barcelona.
		""";

	private static final String exampleResponse = """
		Let’s verify the factual
		accuracy of the derived text step by step:
		1. Evangelos Patoulidis is Regarded as
		One of the Best Players to Emerge from
		Anderlecht Youth:
		- **Derived Text:** Evangelos Patoulidis
		is regarded as one of the best players to
		emerge from Anderlecht youth.
		- **Source Text:** The source text states
		that Patoulidis is regarded as "one of the
		best talents to emerge from Anderlecht’s
		youth set-up".
		- **Verification:** Correct. Rating: 5
		2. He Has Also Attracted Attention from
		Arsenal and Barcelona:
		- **Derived Text:** He has also attracted
		attention from Arsenal and Barcelona.
		- **Source Text:** This fact is mentioned
		verbatim in the source text.
		- **Verification:** Correct. Rating: 5
		3. The Belgian Starlet Rejected a Move
		to Barcelona’s La Masia Academy:
		- **Derived Text:** The Belgian starlet
		rejected a move to Barcelona’s La Masia
		academy.
		- **Source Text:** The source text
		confirms this fact.
		- **Verification:** Correct. Rating: 5
		4. The 14-Year-Old Has Attracted
		Interest from Barcelona to Barcelona:
		- **Derived Text:** The 14-year-old has
		attracted interest from Barcelona to
		Barcelona.
		- **Source Text:** This statement is
		confusing and not supported by the source
		text.
		- **Verification:** Incorrect. Rating: 1
		""";

	public AxcelEvaluator(ChatModel model)
	{
		this.bot = AiServices.create(AxcelBot.class, model);
	}

	@Override
	public Evaluation evaluate(Sample sample)
	{
		ChatMessage exampleUserMsg = UserMessage.from(buildFewShotExemplars(exampleSt, exampleDt));
		ChatMessage exampleResponseAiResponse = AiMessage.from(exampleResponse);
		StringBuilder sb = new StringBuilder();
		for (String context : sample.getContextsListOrFail())
		{
			sb.append(context).append("\n");
		}
		sb.append("User: ").append(sample.getQuestionOrFail());
		String contexts = sb.toString();
		ChatMessage actualUserMsg = UserMessage.from(buildFewShotExemplars(contexts, sample.getAnswerOrFail()));
		log.info("Start Axcel evaluation...");
		String evaluation = bot.evaluate(exampleUserMsg, exampleResponseAiResponse, actualUserMsg);
		log.info("Axcel evaluation completed.");
		// dummy
		return new Evaluation("axcel", 99);
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
}
