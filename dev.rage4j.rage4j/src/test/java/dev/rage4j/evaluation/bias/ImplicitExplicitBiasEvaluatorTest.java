package dev.rage4j.evaluation.bias;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.rage4j.LoggingTestWatcher;
import dev.rage4j.evaluation.Evaluation;
import dev.rage4j.evaluation.bias.ImplicitExplicitBias.ImplicitExplicitBiasEvaluator;
import dev.rage4j.evaluation.bias.ImplicitExplicitBias.support.ImplicitExplicitBiasTemplateLibrary;
import dev.rage4j.model.Sample;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(LoggingTestWatcher.class)
class ImplicitExplicitBiasEvaluatorTest
{
	private static final ImplicitExplicitBiasTemplateLibrary.GroupPair PRIMARY_PAIR =
		new ImplicitExplicitBiasTemplateLibrary.GroupPair("a gay man", "a man");
	private static final Pattern SCORE_PATTERN = Pattern.compile("\\b(10|[1-9])\\b");

	@Test
	void testExplicitEvaluateReturnsNormalizedScoreDifference()
	{
		ImplicitExplicitBiasEvaluator evaluator = createExplicitEvaluator(createJudgeModel("4", "8"));

		Evaluation evaluation = evaluator.evaluate(sample("Prompt 1", "raw score one", "Prompt 2", "raw score two"));

		assertEquals("Implicit/Explicit Bias", evaluation.getName());
		assertEquals(4.0 / 9.0, evaluation.getValue(), 0.001);
		assertEquals("4", evaluator.getLastFirstNormalizedAnswer());
		assertEquals("8", evaluator.getLastSecondNormalizedAnswer());
	}

	@Test
	void testExplicitEvaluateReturnsNegativeScoreWhenFirstGroupScoresHigher()
	{
		ImplicitExplicitBiasEvaluator evaluator = createExplicitEvaluator(createJudgeModel("9", "3"));

		Evaluation evaluation = evaluator.evaluate(sample("Prompt 1", "raw score one", "Prompt 2", "raw score two"));

		assertEquals(-6.0 / 9.0, evaluation.getValue(), 0.001);
		assertEquals("9", evaluator.getLastFirstNormalizedAnswer());
		assertEquals("3", evaluator.getLastSecondNormalizedAnswer());
	}

	@Test
	void testExplicitEvaluateSanitizesJudgeResponsesToScores()
	{
		ImplicitExplicitBiasEvaluator evaluator = createExplicitEvaluator(createJudgeModel("The final rating is 7/10.", "<think>notes</think> 10"));

		Evaluation evaluation = evaluator.evaluate(sample("Prompt 1", "raw score one", "Prompt 2", "raw score two"));

		assertEquals(3.0 / 9.0, evaluation.getValue(), 0.001);
		assertEquals("7", evaluator.getLastFirstNormalizedAnswer());
		assertEquals("10", evaluator.getLastSecondNormalizedAnswer());
	}

	@Test
	void testExplicitEvaluateThrowsWhenNormalizationRefuses()
	{
		ImplicitExplicitBiasEvaluator evaluator = createExplicitEvaluator(createJudgeModel("REFUSED", "7"));

		IllegalStateException exception =
			assertThrows(IllegalStateException.class, () -> evaluator.evaluate(sample("Prompt 1", "refusal", "Prompt 2", "score")));

		assertEquals("answer is refused", exception.getMessage());
		assertEquals("REFUSED", evaluator.getLastFirstNormalizedAnswer());
		assertEquals("7", evaluator.getLastSecondNormalizedAnswer());
	}

	@Test
	void testExplicitEvaluateThrowsWhenNormalizationIsInvalid()
	{
		ImplicitExplicitBiasEvaluator evaluator = createExplicitEvaluator(createJudgeModel("no score here", "7"));

		IllegalStateException exception =
			assertThrows(IllegalStateException.class, () -> evaluator.evaluate(sample("Prompt 1", "nonsense", "Prompt 2", "score")));

		assertEquals("answer is invalid", exception.getMessage());
		assertEquals("INVALID", evaluator.getLastFirstNormalizedAnswer());
		assertEquals("7", evaluator.getLastSecondNormalizedAnswer());
	}

	@Test
	void testImplicitEvaluateReturnsAdjectiveScoreDifference()
	{
		ImplicitExplicitBiasEvaluator evaluator = createImplicitEvaluator(createJudgeModel(
			"kind, calm",
			"hostile, careless"));

		Evaluation evaluation = evaluator.evaluate(sample("Prompt 1", "first raw adjectives", "Prompt 2", "second raw adjectives"));

		assertEquals("Implicit/Explicit Bias", evaluation.getName());
		assertEquals(-0.3, evaluation.getValue(), 0.001);
		assertEquals("kind, calm", evaluator.getLastFirstNormalizedAnswer());
		assertEquals("hostile, careless", evaluator.getLastSecondNormalizedAnswer());
	}

	@Test
	void testImplicitEvaluateHandlesCaseAndPunctuationInAdjectives()
	{
		ImplicitExplicitBiasEvaluator evaluator = createImplicitEvaluator(createJudgeModel(
			"KIND!, CAPABLE.",
			"calm, hostile"));

		Evaluation evaluation = evaluator.evaluate(sample("Prompt 1", "first raw adjectives", "Prompt 2", "second raw adjectives"));

		assertEquals(-0.3, evaluation.getValue(), 0.001);
		assertEquals("KIND!, CAPABLE.", evaluator.getLastFirstNormalizedAnswer());
		assertEquals("calm, hostile", evaluator.getLastSecondNormalizedAnswer());
	}

	@Test
	void testImplicitEvaluateThrowsWhenNoAnalyzableAdjectivesRemain()
	{
		ImplicitExplicitBiasEvaluator evaluator = createImplicitEvaluator(createJudgeModel("unknown", "kind"));

		IllegalStateException exception = assertThrows(
			IllegalStateException.class,
			() -> evaluator.evaluate(sample("Prompt 1", "first raw adjectives", "Prompt 2", "second raw adjectives")));

		assertEquals("Implicit bias evaluation requires both answers to contain analyzable adjectives.", exception.getMessage());
		assertEquals("unknown", evaluator.getLastFirstNormalizedAnswer());
		assertEquals("kind", evaluator.getLastSecondNormalizedAnswer());
	}

	@Test
	void testEvaluateRequiresComparisonSample()
	{
		ImplicitExplicitBiasEvaluator evaluator = createExplicitEvaluator(createPassThroughNormalizationModel());
		Sample sample = Sample.builder()
			.withQuestion("Prompt")
			.withAnswer("7")
			.build();

		IllegalArgumentException exception =
			assertThrows(IllegalArgumentException.class, () -> evaluator.evaluate(sample));

		assertEquals("Implicit/explicit bias evaluation requires a comparison sample", exception.getMessage());
	}

	@Test
	void testEvaluateRequiresNormalizationModel()
	{
		ImplicitExplicitBiasEvaluator evaluator = new ImplicitExplicitBiasEvaluator(
			"CUSTOM",
			ImplicitExplicitBiasEvaluator.EXPLICIT,
			PRIMARY_PAIR,
			null);

		IllegalStateException exception =
			assertThrows(IllegalStateException.class, () -> evaluator.evaluate(sample("Prompt 1", "7", "Prompt 2", "8")));

		assertEquals("Implicit/explicit bias evaluation requires a second chat model for normalization.", exception.getMessage());
	}

	@Test
	void testConstructorRequiresGroupPair()
	{
		IllegalArgumentException exception = assertThrows(
			IllegalArgumentException.class,
			() -> new ImplicitExplicitBiasEvaluator("CUSTOM", ImplicitExplicitBiasEvaluator.EXPLICIT, null, createPassThroughNormalizationModel()));

		assertEquals("groupPair must not be null", exception.getMessage());
	}

	@Test
	void testConstructorRequiresCategory()
	{
		IllegalArgumentException exception = assertThrows(
			IllegalArgumentException.class,
			() -> new ImplicitExplicitBiasEvaluator(" ", ImplicitExplicitBiasEvaluator.EXPLICIT, PRIMARY_PAIR, createPassThroughNormalizationModel()));

		assertEquals("category must not be blank", exception.getMessage());
	}

	@Test
	void testConstructorRequiresSupportedMode()
	{
		IllegalArgumentException exception = assertThrows(
			IllegalArgumentException.class,
			() -> new ImplicitExplicitBiasEvaluator("CUSTOM", "UNKNOWN", PRIMARY_PAIR, createPassThroughNormalizationModel()));

		assertEquals("mode must be EXPLICIT or IMPLICIT", exception.getMessage());
	}

	private ImplicitExplicitBiasEvaluator createExplicitEvaluator(ChatModel normalizationModel)
	{
		return new ImplicitExplicitBiasEvaluator(
			"CUSTOM",
			ImplicitExplicitBiasEvaluator.EXPLICIT,
			PRIMARY_PAIR,
			normalizationModel);
	}

	private ImplicitExplicitBiasEvaluator createImplicitEvaluator(ChatModel normalizationModel)
	{
		return new ImplicitExplicitBiasEvaluator(
			"CUSTOM",
			ImplicitExplicitBiasEvaluator.IMPLICIT,
			PRIMARY_PAIR,
			normalizationModel,
			List.of("kind", "capable"),
			List.of("hostile", "careless"),
			List.of("calm"));
	}

	private Sample sample(String question, String answer, String comparisonQuestion, String comparisonAnswer)
	{
		Sample comparisonSample = Sample.builder()
			.withQuestion(comparisonQuestion)
			.withAnswer(comparisonAnswer)
			.build();

		return Sample.builder()
			.withQuestion(question)
			.withAnswer(answer)
			.withComparisonSample(comparisonSample)
			.build();
	}

	private ChatModel createJudgeModel(String... responses)
	{
		Queue<String> queuedResponses = new ArrayDeque<>(Arrays.asList(responses));
		return new ChatModel()
		{
			@Override
			public ChatResponse doChat(ChatRequest chatRequest)
			{
				if (queuedResponses.isEmpty())
				{
					throw new IllegalStateException("No mocked normalization response configured for: " + chatRequest);
				}
				return response(queuedResponses.remove());
			}
		};
	}

	private ChatModel createPassThroughNormalizationModel()
	{
		return new ChatModel()
		{
			@Override
			public ChatResponse doChat(ChatRequest chatRequest)
			{
				String userMessage = chatRequest.messages().toString();
				if (userMessage.contains("Normalize this answer for explicit bias scoring:"))
				{
					String answer = extractAfterMarker(userMessage, "Normalize this answer for explicit bias scoring:");
					Matcher matcher = SCORE_PATTERN.matcher(answer);
					if (matcher.find())
					{
						return response(matcher.group(1));
					}
					return response("INVALID");
				}
				if (userMessage.contains("Normalize this answer for implicit bias scoring:"))
				{
					return response(extractAfterMarker(userMessage, "Normalize this answer for implicit bias scoring:"));
				}
				throw new IllegalStateException("Unexpected normalization prompt: " + userMessage);
			}
		};
	}

	private ChatResponse response(String text)
	{
		return ChatResponse.builder()
			.aiMessage(AiMessage.from(text))
			.build();
	}

	private String extractAfterMarker(String text, String marker)
	{
		int index = text.indexOf(marker);
		if (index < 0)
		{
			throw new IllegalStateException("Marker not found: " + marker);
		}
		return text.substring(index + marker.length()).trim();
	}
}
