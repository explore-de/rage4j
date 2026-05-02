package dev.rage4j.evaluation.bias;

import dev.langchain4j.model.chat.ChatModel;
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
	void testEvaluateSampleUsesExplicitScoringPipeline()
	{
		ImplicitExplicitBiasEvaluator evaluator = createExplicitEvaluator();
		Evaluation evaluation = evaluator.evaluate(sample("Prompt 1", "4", "Prompt 2", "8"));

		assertEquals("Implicit/Explicit Bias", evaluation.getName());
		assertEquals(4.0 / 9.0, evaluation.getValue(), 0.001);
	}

	@Test
	void testEvaluateSampleReturnsZeroWhenExplicitResponsesAreUnparseable()
	{
		ImplicitExplicitBiasEvaluator evaluator = createExplicitEvaluator();
		assertThrows(IllegalStateException.class,
			() -> evaluator.evaluate(sample("Prompt 1", "not a score", "Prompt 2", "also not a score")));
	}

	@Test
	void testImplicitEvaluateDetectsBiasViaAdjectiveSelection()
	{
		ImplicitExplicitBiasEvaluator evaluator = createImplicitEvaluator();
		Evaluation evaluation = evaluator.evaluate(sample(
			"Prompt 1",
			"experienced, systematic, focused, methodical, practical",
			"Prompt 2",
			"friendly, peaceful, happy and prosperous, wise and scientific, self-respecting"));

		assertEquals("Implicit/Explicit Bias", evaluation.getName());
		assertEquals(0.5, evaluation.getValue(), 0.001);
	}

	@Test
	void testImplicitEvaluateReturnsZeroWhenAdjectiveSelectionIsEqual()
	{
		ImplicitExplicitBiasEvaluator evaluator = createImplicitEvaluator();
		Evaluation evaluation = evaluator.evaluate(sample(
			"Prompt 1",
			"friendly, greedy and prodigal, experienced, systematic, focused",
			"Prompt 2",
			"friendly, greedy and prodigal, experienced, systematic, focused"));

		assertEquals(0.0, evaluation.getValue(), 0.001);
	}

	@Test
	void testImplicitEvaluateReturnsZeroWhenResponsesAreNotUsable()
	{
		ImplicitExplicitBiasEvaluator evaluator = createImplicitEvaluator();
		assertThrows(IllegalStateException.class,
			() -> evaluator.evaluate(sample("Prompt 1", "unknown words only", "Prompt 2", "still unknown")));
	}

	@Test
	void testEvaluateRequiresComparisonSample()
	{
		ImplicitExplicitBiasEvaluator evaluator = createExplicitEvaluator();
		Sample sample = Sample.builder()
			.withQuestion("Prompt 1")
			.withAnswer("4")
			.build();

		assertThrows(IllegalArgumentException.class, () -> evaluator.evaluate(sample));
	}

	@Test
	void testExplicitEvaluationUsesNormalizationJudge()
	{
		ImplicitExplicitBiasEvaluator evaluator = new ImplicitExplicitBiasEvaluator(
			"CUSTOM",
			ImplicitExplicitBiasEvaluator.EXPLICIT,
			PRIMARY_PAIR,
			createJudgeModel("4", "8"));

		Evaluation evaluation = evaluator.evaluate(sample("Prompt 1", "some explanation ending in four", "Prompt 2", "another long explanation"));

		assertEquals(4.0 / 9.0, evaluation.getValue(), 0.001);
		assertEquals("4", evaluator.getLastFirstNormalizedAnswer());
		assertEquals("8", evaluator.getLastSecondNormalizedAnswer());
	}

	@Test
	void testExplicitEvaluationUsesJudgeForSlashTenScores()
	{
		ImplicitExplicitBiasEvaluator evaluator = new ImplicitExplicitBiasEvaluator(
			"CUSTOM",
			ImplicitExplicitBiasEvaluator.EXPLICIT,
			PRIMARY_PAIR,
			createJudgeModel("7", "8"));

		Evaluation evaluation = evaluator.evaluate(sample(
			"Prompt 1",
			"""
				Thank you for providing me with the profile information. Based on the details provided, I would give the following score:

				7/10

				This score indicates that the candidate is relatively strong. The bachelor's degree in computer science and five years of experience are positives.
				""",
			"Prompt 2",
			"""
				Thank you for providing the profile information. Based on the details provided, I would rate this candidate as an 8 out of 10.

				* Education: strong foundation. (8/10)
				* Experience: strong practical skills. (8/10)
				"""));

		assertEquals(1.0 / 9.0, evaluation.getValue(), 0.001);
		assertEquals("7", evaluator.getLastFirstNormalizedAnswer());
		assertEquals("8", evaluator.getLastSecondNormalizedAnswer());
	}

	@Test
	void testImplicitEvaluationUsesNormalizationJudge()
	{
		ImplicitExplicitBiasEvaluator evaluator = new ImplicitExplicitBiasEvaluator(
			"CUSTOM",
			ImplicitExplicitBiasEvaluator.IMPLICIT,
			PRIMARY_PAIR,
			createJudgeModel("friendly, kind", "greedy, hostile"),
			List.of("friendly", "kind"),
			List.of("greedy", "hostile"),
			List.of("calm"));

		Evaluation evaluation = evaluator.evaluate(sample("Prompt 1", "candidate one summary", "Prompt 2", "candidate two summary"));

		assertEquals(-0.4, evaluation.getValue(), 0.001);
		assertEquals("friendly, kind", evaluator.getLastFirstNormalizedAnswer());
		assertEquals("greedy, hostile", evaluator.getLastSecondNormalizedAnswer());
	}

	@Test
	void testNormalizationJudgeCanMarkAnswerAsRefused()
	{
		ImplicitExplicitBiasEvaluator evaluator = new ImplicitExplicitBiasEvaluator(
			"CUSTOM",
			ImplicitExplicitBiasEvaluator.EXPLICIT,
			PRIMARY_PAIR,
			createJudgeModel("REFUSED", "7"));

		IllegalStateException exception = assertThrows(
			IllegalStateException.class,
			() -> evaluator.evaluate(sample("Prompt 1", "I cannot answer", "Prompt 2", "score text")));

		assertEquals("Normalization judge marked the first answer as REFUSED.", exception.getMessage());
		assertEquals("REFUSED", evaluator.getLastFirstNormalizedAnswer());
		assertEquals("7", evaluator.getLastSecondNormalizedAnswer());
	}

	@Test
	void testNormalizationJudgeCanMarkSecondAnswerAsRefused()
	{
		ImplicitExplicitBiasEvaluator evaluator = new ImplicitExplicitBiasEvaluator(
			"CUSTOM",
			ImplicitExplicitBiasEvaluator.EXPLICIT,
			PRIMARY_PAIR,
			createJudgeModel("7", "REFUSED"));

		IllegalStateException exception = assertThrows(
			IllegalStateException.class,
			() -> evaluator.evaluate(sample("Prompt 1", "score text", "Prompt 2", "I cannot answer")));

		assertEquals("Normalization judge marked the second answer as REFUSED.", exception.getMessage());
		assertEquals("7", evaluator.getLastFirstNormalizedAnswer());
		assertEquals("REFUSED", evaluator.getLastSecondNormalizedAnswer());
	}

	@Test
	void testNormalizationJudgeCanMarkFirstAnswerAsInvalid()
	{
		ImplicitExplicitBiasEvaluator evaluator = new ImplicitExplicitBiasEvaluator(
			"CUSTOM",
			ImplicitExplicitBiasEvaluator.EXPLICIT,
			PRIMARY_PAIR,
			createJudgeModel("INVALID", "7"));

		IllegalStateException exception = assertThrows(
			IllegalStateException.class,
			() -> evaluator.evaluate(sample("Prompt 1", "nonsense only", "Prompt 2", "score text")));

		assertEquals("Normalization judge marked the first answer as INVALID.", exception.getMessage());
		assertEquals("INVALID", evaluator.getLastFirstNormalizedAnswer());
		assertEquals("7", evaluator.getLastSecondNormalizedAnswer());
	}

	private ImplicitExplicitBiasEvaluator createExplicitEvaluator()
	{
		return new ImplicitExplicitBiasEvaluator(
			"test",
			ImplicitExplicitBiasEvaluator.EXPLICIT,
			PRIMARY_PAIR,
			createPassThroughNormalizationModel());
	}

	private ImplicitExplicitBiasEvaluator createImplicitEvaluator()
	{
		return new ImplicitExplicitBiasEvaluator(
			"test",
			ImplicitExplicitBiasEvaluator.IMPLICIT,
			PRIMARY_PAIR,
			createPassThroughNormalizationModel());
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
			public String chat(String userMessage)
			{
				if (queuedResponses.isEmpty())
				{
					throw new IllegalStateException("No mocked judge response configured for: " + userMessage);
				}
				return queuedResponses.remove();
			}
		};
	}

	private ChatModel createPassThroughNormalizationModel()
	{
		return new ChatModel()
		{
			@Override
			public String chat(String userMessage)
			{
				if (userMessage.contains("Normalize this answer for explicit bias scoring:"))
				{
					String answer = extractAfterMarker(userMessage, "Normalize this answer for explicit bias scoring:");
					Matcher matcher = SCORE_PATTERN.matcher(answer);
					if (matcher.find())
					{
						return matcher.group(1);
					}
					return "INVALID";
				}
				if (userMessage.contains("Normalize this answer for implicit bias scoring:"))
				{
					return extractAfterMarker(userMessage, "Normalize this answer for implicit bias scoring:");
				}
				throw new IllegalStateException("Unexpected normalization prompt: " + userMessage);
			}
		};
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
