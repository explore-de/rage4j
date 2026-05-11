package dev.rage4j.evaluation.bias.implicitexplicit;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.rage4j.LoggingTestWatcher;
import dev.rage4j.evaluation.Evaluation;
import dev.rage4j.evaluation.bias.implicitexplicit.support.ImplicitExplicitTemplateLibrary.GroupPair;
import dev.rage4j.model.Sample;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(LoggingTestWatcher.class)
class ImplicitExplicitBiasEvaluatorTest
{
	private static final GroupPair GROUP_PAIR = new GroupPair("a gay man", "a man");

	@Test
	void testExplicitEvaluateNormalizesAndScoresSignedDifference()
	{
		ImplicitExplicitEvaluator evaluator = createExplicitEvaluator(createJudgeModel("The final rating is 4/10.", "8"));

		Evaluation evaluation = evaluator.evaluate(sample("Prompt 1", "raw score one", "Prompt 2", "raw score two"));

		assertEquals("Explicit Bias", evaluation.getName());
		assertEquals(4.0 / 9.0, evaluation.getValue(), 0.001);
		assertEquals("4", evaluator.getLastFirstNormalizedAnswer());
		assertEquals("8", evaluator.getLastSecondNormalizedAnswer());
	}

	@Test
	void testImplicitEvaluateNormalizesAndScoresAdjectiveDifference()
	{
		ImplicitExplicitEvaluator evaluator = createImplicitEvaluator(createJudgeModel("kind, calm", "hostile, careless"));

		Evaluation evaluation = evaluator.evaluate(sample("Prompt 1", "first raw adjectives", "Prompt 2", "second raw adjectives"));

		assertEquals("Implicit Bias", evaluation.getName());
		assertEquals(-0.3, evaluation.getValue(), 0.001);
		assertEquals("kind, calm", evaluator.getLastFirstNormalizedAnswer());
		assertEquals("hostile, careless", evaluator.getLastSecondNormalizedAnswer());
	}

	@Test
	void testEvaluateRejectsRefusedNormalizedAnswer()
	{
		ImplicitExplicitEvaluator evaluator = createExplicitEvaluator(createJudgeModel("REFUSED", "7"));

		IllegalStateException exception = assertThrows(IllegalStateException.class, () -> evaluator.evaluate(sample("Prompt 1", "refusal", "Prompt 2", "score")));
		assertEquals("answer is refused", exception.getMessage());
		assertEquals("REFUSED", evaluator.getLastFirstNormalizedAnswer());
	}

	@Test
	void testEvaluateRejectsInvalidNormalizedAnswer()
	{
		ImplicitExplicitEvaluator evaluator = createExplicitEvaluator(createJudgeModel("no score here", "7"));

		IllegalStateException exception = assertThrows(IllegalStateException.class, () -> evaluator.evaluate(sample("Prompt 1", "nonsense", "Prompt 2", "score")));
		assertEquals("answer is invalid", exception.getMessage());
		assertEquals("INVALID", evaluator.getLastFirstNormalizedAnswer());
	}

	@Test
	void testEvaluateRequiresComparisonSample()
	{
		ImplicitExplicitEvaluator evaluator = createExplicitEvaluator(createJudgeModel("7"));
		Sample sample = Sample.builder()
			.withQuestion("Prompt")
			.withAnswer("7")
			.build();

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> evaluator.evaluate(sample));
		assertEquals("Implicit/explicit bias evaluation requires a comparison sample", exception.getMessage());
	}

	@Test
	void testEvaluateRequiresNormalizationModel()
	{
		ImplicitExplicitEvaluator evaluator = new ImplicitExplicitEvaluator(null, ImplicitExplicitEvaluator.EXPLICIT, GROUP_PAIR, null);

		IllegalStateException exception = assertThrows(IllegalStateException.class, () -> evaluator.evaluate(sample("Prompt 1", "7", "Prompt 2", "8")));
		assertEquals("Implicit/explicit bias evaluation requires a second chat model for normalization.", exception.getMessage());
	}

	@Test
	void testConstructorValidatesRequiredConfiguration()
	{
		ChatModel model = createJudgeModel("7", "8");

		assertEquals("groupPair must not be null", assertThrows(IllegalArgumentException.class, () -> new ImplicitExplicitEvaluator(null, ImplicitExplicitEvaluator.EXPLICIT, null, model))
			.getMessage());
		assertEquals("mode must be EXPLICIT or IMPLICIT", assertThrows(IllegalArgumentException.class, () -> new ImplicitExplicitEvaluator(null, "UNKNOWN", GROUP_PAIR, model))
			.getMessage());
	}

	@Test
	void testImplicitConstructorRejectsMissingCategoryAndAdjectives()
	{
		ChatModel model = createJudgeModel("kind", "hostile");

		assertEquals("Implicit bias evaluation requires either a supported category or user-provided adjectives.",
			assertThrows(IllegalArgumentException.class, () -> new ImplicitExplicitEvaluator(" ", ImplicitExplicitEvaluator.IMPLICIT, GROUP_PAIR, model))
				.getMessage());
		assertEquals("Unsupported adjective category: UNKNOWN",
			assertThrows(IllegalArgumentException.class, () -> new ImplicitExplicitEvaluator("UNKNOWN", ImplicitExplicitEvaluator.IMPLICIT, GROUP_PAIR, model))
				.getMessage());
	}

	@Test
	void testImplicitConstructorAllowsUserProvidedAdjectivesWithoutCategory()
	{
		ImplicitExplicitEvaluator evaluator = new ImplicitExplicitEvaluator(null, ImplicitExplicitEvaluator.IMPLICIT, GROUP_PAIR,
			createJudgeModel("kind, calm", "hostile"), List.of("kind"), List.of("hostile"), List.of("calm"));

		Evaluation evaluation = evaluator.evaluate(sample("Prompt 1", "first raw adjectives", "Prompt 2", "second raw adjectives"));

		assertEquals("Implicit Bias", evaluation.getName());
		assertEquals(-0.2, evaluation.getValue(), 0.001);
	}

	private ImplicitExplicitEvaluator createExplicitEvaluator(ChatModel normalizationModel)
	{
		return new ImplicitExplicitEvaluator(null, ImplicitExplicitEvaluator.EXPLICIT, GROUP_PAIR, normalizationModel);
	}

	private ImplicitExplicitEvaluator createImplicitEvaluator(ChatModel normalizationModel)
	{
		return new ImplicitExplicitEvaluator(null, ImplicitExplicitEvaluator.IMPLICIT, GROUP_PAIR, normalizationModel, List.of("kind", "capable"), List.of("hostile", "careless"), List.of("calm"));
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
				return ChatResponse.builder()
					.aiMessage(AiMessage.from(queuedResponses.remove()))
					.build();
			}
		};
	}
}
