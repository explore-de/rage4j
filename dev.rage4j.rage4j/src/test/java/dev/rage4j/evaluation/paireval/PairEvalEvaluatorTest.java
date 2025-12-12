package dev.rage4j.evaluation.paireval;

import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.rage4j.LoggingTestWatcher;
import dev.rage4j.evaluation.Evaluation;
import dev.rage4j.model.Sample;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(LoggingTestWatcher.class)
class PairEvalEvaluatorTest
{
	private static final String OPEN_AI_KEY = System.getenv("OPEN_AI_KEY");
	private PairEvalEvaluator evaluator;

	@BeforeEach
	void setup()
	{
		OpenAiChatModel model = OpenAiChatModel.builder()
			//.logRequests(true)
			//.logResponses(true)
			.apiKey(OPEN_AI_KEY)
			.modelName("gpt-4.1")
			.temperature(0.0)
			.build();
		evaluator = new PairEvalEvaluator(model);
	}

	@Test
	@Tag("integration")
	void testEvaluate()
	{
		String context = """
			User: Hello, my name is John.
			AI: Hello John, how can I assist you today?
			User: Can you tell me a joke?
			AI: Sure! Why don't scientists trust atoms? Because they make up everything!""";

		Sample sample = Sample.builder()
			.withQuestion("Good one! Do you remember my name?")
			.withAnswer("Yes, your name is John.")
			.withContext(context)
			.build();

		Evaluation evaluation = evaluator.evaluate(sample);
		assertTrue(evaluation.getValue() >= 0.8);
	}

	@Test
	@Tag("integration")
	void testEvaluateFail()
	{
		String context = """
			User: Hello, my name is John.
			AI: Hello John, how can I assist you today?
			User: Can you tell me a joke?
			AI: Sure! Why don't scientists trust atoms? Because they make up everything!""";

		Sample sample = Sample.builder()
			.withQuestion("Good one! Do you remember my name?")
			.withAnswer("Ready for a next joke?")
			.withContext(context)
			.build();

		Evaluation evaluation = evaluator.evaluate(sample);
		//assertTrue(evaluation.getValue() <= 0.5);
		assertTrue(evaluation.getValue() <= 0.33);
	}
}