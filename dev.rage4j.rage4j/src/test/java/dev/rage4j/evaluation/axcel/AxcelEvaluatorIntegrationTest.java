package dev.rage4j.evaluation.axcel;

import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.rage4j.LoggingTestWatcher;
import dev.rage4j.evaluation.Evaluation;
import dev.rage4j.model.Sample;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static dev.langchain4j.model.chat.Capability.RESPONSE_FORMAT_JSON_SCHEMA;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(LoggingTestWatcher.class)
public class AxcelEvaluatorIntegrationTest
{
	private static final String OPEN_AI_KEY = System.getenv("OPEN_AI_KEY");
	private AxcelEvaluator evaluator;

	@BeforeEach
	void setUp()
	{
		OpenAiChatModel model = OpenAiChatModel.builder()
			.logRequests(true)
			.logResponses(true)
			.apiKey(OPEN_AI_KEY)
			.modelName("gpt-4.1")
			.supportedCapabilities(RESPONSE_FORMAT_JSON_SCHEMA)
			.strictJsonSchema(true)
			.temperature(0.0)
			.build();
		evaluator = new AxcelEvaluator(model);
	}

	@Test
	@Tag("integration")
	void testEvaluate()
	{
		List<String> context = List.of("User: Hello, my name is John.",
			"AI: Hello John, how can I assist you today?",
			"User: Can you tell me a joke?",
			"AI: Sure! Why don't scientists trust atoms? Because they make up everything!");

		Sample sample = Sample.builder()
			.withQuestion("Good one! Do you know any facts about space?")
			.withAnswer("Sure thing John! Here are some facts about space...")
			.withContextsList(context)
			.build();

		Evaluation evaluation = evaluator.evaluate(sample);
		assertTrue(evaluation.getValue() >= 0.6);
	}
}
