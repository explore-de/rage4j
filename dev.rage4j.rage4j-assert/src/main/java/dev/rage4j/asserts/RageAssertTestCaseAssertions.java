package dev.rage4j.asserts;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.rage4j.asserts.exception.Rage4JBleuScoreException;
import dev.rage4j.asserts.exception.Rage4JCorrectnessException;
import dev.rage4j.asserts.exception.Rage4JFaithfulnessException;
import dev.rage4j.asserts.exception.Rage4JImplicitExplicitException;
import dev.rage4j.asserts.exception.Rage4JRefusalException;
import dev.rage4j.asserts.exception.Rage4JRelevanceException;
import dev.rage4j.asserts.exception.Rage4JRougeScoreException;
import dev.rage4j.asserts.exception.Rage4JSimilarityException;
import dev.rage4j.evaluation.Evaluation;
import dev.rage4j.evaluation.answercorrectness.AnswerCorrectnessEvaluator;
import dev.rage4j.evaluation.answerrelevance.AnswerRelevanceEvaluator;
import dev.rage4j.evaluation.answersemanticsimilarity.AnswerSemanticSimilarityEvaluator;
import dev.rage4j.evaluation.bleuscore.BleuScoreEvaluator;
import dev.rage4j.evaluation.bias.implicitexplicit.ImplicitExplicitEvaluator;
import dev.rage4j.evaluation.bias.implicitexplicit.support.ImplicitExplicitTemplateLibrary;
import dev.rage4j.evaluation.bias.implicitexplicit.support.ImplicitExplicitTemplateLibrary.AdjectivePreset;
import dev.rage4j.evaluation.bias.implicitexplicit.support.ImplicitExplicitTemplateLibrary.GroupPair;
import dev.rage4j.evaluation.bias.refusal.RefusalEvaluator;
import dev.rage4j.evaluation.faithfulness.FaithfulnessEvaluator;
import dev.rage4j.evaluation.rougescore.RougeScoreEvaluator;
import dev.rage4j.model.Sample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class RageAssertTestCaseAssertions
{
	private static final Logger LOG = LoggerFactory.getLogger(RageAssertTestCaseAssertions.class);
	private static final String MINVALUE = "Answer did not reach required min value! Evaluated value: ";
	private static final int MAXRETRYCOUNT = 3;

	private final EmbeddingModel embeddingModel;
	private final String question;
	private final String groundTruth;
	private final String context;
	private final String answer;
	private final String comparisonQuestion;
	private final String comparisonGroundTruth;
	private final String comparisonContext;
	private final String comparisonAnswer;
	private final ImplicitExplicitScenario implicitExplicitScenario;
	private final ChatModel judgeChatModel;
	private final ChatModel evaluatedChatModel;

	public RageAssertTestCaseAssertions(String answer, String groundTruth, String question, String context, String comparisonAnswer, String comparisonGroundTruth, String comparisonQuestion, String comparisonContext, ImplicitExplicitScenario implicitExplicitScenario, ChatModel judgeChatModel, ChatModel evaluatedChatModel, EmbeddingModel embeddingModel)
	{
		this.answer = answer;
		this.groundTruth = groundTruth;
		this.question = question;
		this.context = context;
		this.comparisonAnswer = comparisonAnswer;
		this.comparisonGroundTruth = comparisonGroundTruth;
		this.comparisonQuestion = comparisonQuestion;
		this.comparisonContext = comparisonContext;
		this.implicitExplicitScenario = implicitExplicitScenario;
		this.judgeChatModel = judgeChatModel;
		this.evaluatedChatModel = evaluatedChatModel;
		this.embeddingModel = embeddingModel;
	}

	public AssertionEvaluation assertFaithfulness(double minValue)
	{
		FaithfulnessEvaluator evaluator = new FaithfulnessEvaluator(evaluatedChatModel);
		Sample sample = Sample.builder()
			.withAnswer(answer)
			.withGroundTruth(groundTruth)
			.withQuestion(question)
			.withContext(context)
			.build();
		Evaluation evaluation = evaluator.evaluate(sample);

		if (minValue > evaluation.getValue())
		{
			throw new Rage4JFaithfulnessException(MINVALUE + evaluation.getValue() + " answer: " + answer);
		}
		return AssertionEvaluation.from(evaluation, this);
	}

	public AssertionEvaluation assertAnswerCorrectness(double minValue)
	{
		AnswerCorrectnessEvaluator evaluator = new AnswerCorrectnessEvaluator(evaluatedChatModel);
		Sample sample = Sample.builder()
			.withAnswer(answer)
			.withGroundTruth(groundTruth)
			.withQuestion(question)
			.build();
		Evaluation evaluation = evaluator.evaluate(sample);

		if (minValue > evaluation.getValue())
		{
			throw new Rage4JCorrectnessException(MINVALUE + evaluation.getValue() + " answer: " + answer);
		}
		return AssertionEvaluation.from(evaluation, this);
	}

	public AssertionEvaluation assertAnswerRelevance(double minValue)
	{
		AnswerRelevanceEvaluator evaluator = new AnswerRelevanceEvaluator(evaluatedChatModel, embeddingModel);
		Sample sample = Sample.builder()
			.withAnswer(answer)
			.withQuestion(question)
			.withContext(context)
			.build();
		Evaluation evaluation = evaluator.evaluate(sample);

		if (minValue > evaluation.getValue())
		{
			throw new Rage4JRelevanceException(MINVALUE + evaluation.getValue() + ", Required: " + minValue + ", Answer: " + answer);
		}
		return AssertionEvaluation.from(evaluation, this);
	}

	public AssertionEvaluation assertSemanticSimilarity(double minValue)
	{
		AnswerSemanticSimilarityEvaluator evaluator = new AnswerSemanticSimilarityEvaluator(embeddingModel);
		Sample sample = Sample.builder()
			.withAnswer(answer)
			.withGroundTruth(groundTruth)
			.build();
		Evaluation evaluation = evaluator.evaluate(sample);

		if (minValue > evaluation.getValue())
		{
			throw new Rage4JSimilarityException(MINVALUE + evaluation.getValue() + ", Required: " + minValue + ", Answer: " + answer);
		}
		return AssertionEvaluation.from(evaluation, this);
	}

	public AssertionEvaluation assertBleuScore(double minValue)
	{
		BleuScoreEvaluator evaluator = new BleuScoreEvaluator();
		Sample sample = Sample.builder()
			.withAnswer(answer)
			.withGroundTruth(groundTruth)
			.build();
		Evaluation evaluation = evaluator.evaluate(sample);

		if (minValue > evaluation.getValue())
		{
			throw new Rage4JBleuScoreException(MINVALUE + evaluation.getValue() + ", Required: " + minValue + ", Answer: " + answer);
		}
		return AssertionEvaluation.from(evaluation, this);
	}

	public AssertionEvaluation assertRougeScore(double minValue, RougeScoreEvaluator.RougeType rougeType, RougeScoreEvaluator.MeasureType measureType)
	{
		RougeScoreEvaluator evaluator = new RougeScoreEvaluator(rougeType, measureType);
		Sample sample = Sample.builder()
			.withAnswer(answer)
			.withGroundTruth(groundTruth)
			.build();
		Evaluation evaluation = evaluator.evaluate(sample);

		if (minValue > evaluation.getValue())
		{
			throw new Rage4JRougeScoreException(MINVALUE + evaluation.getValue() + ", Required: " + minValue + ", Answer: " + answer);
		}
		return AssertionEvaluation.from(evaluation, this);
	}

	public AssertionEvaluation assertRefusalDisparity()
	{
		return assertRefusalDisparity(null, 1);
	}

	public AssertionEvaluation assertRefusalDisparity(double minValue)
	{
		return assertRefusalDisparity(minValue, 1);
	}

	public AssertionEvaluation assertRefusalDisparity(int runs)
	{
		return assertRefusalDisparity(null, runs);
	}

	public AssertionEvaluation assertRefusalDisparity(double minValue, int runs)
	{
		return assertRefusalDisparity(Double.valueOf(minValue), runs);
	}

	private AssertionEvaluation assertRefusalDisparity(Double minValue, int runs)
	{
		if (runs <= 0)
		{
			throw new IllegalArgumentException("runs must be greater than 0");
		}

		RefusalEvaluator evaluator = new RefusalEvaluator(judgeChatModel);
		double totalScore = 0.0;
		int unfairRuns = 0;
		int successfullRuns = 0;
		int firstRefusals = 0;
		int secondRefusals = 0;

		// for better logging we skip evaluate method
		for (int run = 0; run < runs; run++)
		{
			RefusalEvaluator.DisparityState comparison = evaluateRefusalRun(evaluator, runs);

			if (comparison == null)
			{
				continue;
			}

			successfullRuns++;

			if (evaluator.getLastFirstState() == RefusalEvaluator.RefusalState.REFUSAL)
			{
				firstRefusals++;
			}
			if (evaluator.getLastSecondState() == RefusalEvaluator.RefusalState.REFUSAL)
			{
				secondRefusals++;
			}
			if (comparison == RefusalEvaluator.DisparityState.DISPARITY)
			{
				// and manually add 1.0
				totalScore += 1.0;
				unfairRuns++;
			}
		}

		if (successfullRuns == 0)
		{
			throw new IllegalStateException("Refusal disparity assertion produced no successful runs.");
		}

		double averageScore = totalScore / successfullRuns;
		Evaluation evaluation = new Evaluation("Refusal Disparity", averageScore);

		LOG.info("");
		LOG.info("Refusal disparity aggregate: firstRefusals={}, secondRefusals={}, configuredRuns={}, successfullRuns={}, unfairRuns={}, averageScore={}", firstRefusals, secondRefusals, runs, successfullRuns, unfairRuns, averageScore);

		if (minValue != null && minValue > evaluation.getValue())
		{
			throw new Rage4JRefusalException(MINVALUE + evaluation.getValue() + ", Required: " + minValue + ", Answer: " + answer);
		}
		return AssertionEvaluation.from(evaluation, this);
	}

	public AssertionEvaluation assertImplicitExplicit()
	{
		return assertImplicitExplicit(null, 1);
	}

	public AssertionEvaluation assertImplicitExplicit(double minValue)
	{
		return assertImplicitExplicit(Double.valueOf(minValue), 1);
	}

	public AssertionEvaluation assertImplicitExplicit(int runs)
	{
		return assertImplicitExplicit(null, runs);
	}

	public AssertionEvaluation assertImplicitExplicit(double minValue, int runs)
	{
		return assertImplicitExplicit(Double.valueOf(minValue), runs);
	}

	private AssertionEvaluation assertImplicitExplicit(Double minValue, int runs)
	{
		if (implicitExplicitScenario == null)
		{
			throw new IllegalStateException("An implicitExplicitScenario must be set before assertImplicitExplicitBias is used.");
		}

		// are there custom adjectives along the scenario object?
		AdjectivePreset adjectivePreset = implicitExplicitScenario.adjectivePreset();
		if (adjectivePreset != null)
		{
			return assertImplicitExplicitInternal(implicitExplicitScenario.effectiveCategory(), implicitExplicitScenario.mode().value(), minValue, runs, implicitExplicitScenario.groupPair(), adjectivePreset.positiveAdjectives(), adjectivePreset.negativeAdjectives(), adjectivePreset.neutralAdjectives());
		}
		return assertImplicitExplicitInternal(implicitExplicitScenario.effectiveCategory(), implicitExplicitScenario.mode().value(), minValue, runs, implicitExplicitScenario.groupPair(), null, null, null);
	}

	public AssertionEvaluation assertImplicitExplicit(GroupPair groupPair, String mode, double minValue)
	{
		return assertImplicitExplicit("CUSTOM", mode, minValue, 1, groupPair);
	}

	public AssertionEvaluation assertImplicitExplicit(GroupPair groupPair, String mode)
	{
		return assertImplicitExplicitInternal("CUSTOM", mode, null, 1, groupPair, null, null, null);
	}

	public AssertionEvaluation assertImplicitExplicit(String category, String mode, double minValue, GroupPair groupPair)
	{
		return assertImplicitExplicitInternal(category, mode, minValue, 1, groupPair, null, null, null);
	}

	public AssertionEvaluation assertImplicitExplicit(String category, String mode, GroupPair groupPair)
	{
		return assertImplicitExplicitInternal(category, mode, null, 1, groupPair, null, null, null);
	}

	public AssertionEvaluation assertImplicitExplicit(GroupPair groupPair, String mode, double minValue, int runs)
	{
		return assertImplicitExplicit("CUSTOM", mode, minValue, runs, groupPair);
	}

	public AssertionEvaluation assertImplicitExplicit(GroupPair groupPair, String mode, int runs)
	{
		return assertImplicitExplicitInternal("CUSTOM", mode, null, runs, groupPair, null, null, null);
	}

	public AssertionEvaluation assertImplicitExplicit(GroupPair groupPair, String mode, List<String> positiveAdjectives, List<String> negativeAdjectives, List<String> neutralAdjectives)
	{
		return assertImplicitExplicitInternal("CUSTOM", mode, null,
			1, groupPair, positiveAdjectives, negativeAdjectives, neutralAdjectives);
	}

	public AssertionEvaluation assertImplicitExplicit(GroupPair groupPair, String mode, int runs, List<String> positiveAdjectives, List<String> negativeAdjectives, List<String> neutralAdjectives)
	{
		return assertImplicitExplicitInternal("CUSTOM", mode, null, runs,
			groupPair, positiveAdjectives, negativeAdjectives, neutralAdjectives);
	}

	public AssertionEvaluation assertImplicitExplicit(GroupPair groupPair, String mode, double minValue, int runs, List<String> positiveAdjectives, List<String> negativeAdjectives, List<String> neutralAdjectives)
	{
		return assertImplicitExplicit("CUSTOM", mode, minValue, runs, groupPair,
			positiveAdjectives, negativeAdjectives, neutralAdjectives);
	}

	public AssertionEvaluation assertImplicitExplicit(String category, String mode, double minValue, int runs, GroupPair groupPair)
	{
		return assertImplicitExplicit(category, mode, minValue, runs, groupPair, null, null, null);
	}

	public AssertionEvaluation assertImplicitExplicit(String category, String mode, int runs, GroupPair groupPair)
	{
		return assertImplicitExplicitInternal(category, mode, null, runs, groupPair, null, null, null);
	}

	public AssertionEvaluation assertImplicitExplicit(String category, String mode, double minValue, int runs, GroupPair groupPair, List<String> positiveAdjectives, List<String> negativeAdjectives, List<String> neutralAdjectives)
	{
		return assertImplicitExplicitInternal(category, mode, minValue, runs, groupPair, positiveAdjectives, negativeAdjectives, neutralAdjectives);
	}

	private AssertionEvaluation assertImplicitExplicitInternal(String category, String mode, Double minValue, int runs, GroupPair groupPair, List<String> positiveAdjectives, List<String> negativeAdjectives, List<String> neutralAdjectives)
	{
		if (runs <= 0)
		{
			throw new IllegalArgumentException("runs must be greater than 0");
		}
		if (groupPair == null)
		{
			throw new IllegalStateException("A groupPair is required for implicit/explicit bias assertions.");
		}
		if (judgeChatModel == null)
		{
			throw new IllegalStateException("Implicit/explicit bias assertion requires a judge chat model for normalization.");
		}

		ImplicitExplicitEvaluator evaluator = new ImplicitExplicitEvaluator(category, mode, groupPair, judgeChatModel, positiveAdjectives, negativeAdjectives, neutralAdjectives);

		double totalBiasScore = 0.0;
		int validRuns = 0;
		Map<String, Integer> wordCounts = new HashMap<>();
		Set<String> allowedAdjectives = Set.of();

		if (ImplicitExplicitEvaluator.IMPLICIT.equals(mode))
		{
			AdjectivePreset adjectivePreset = ImplicitExplicitTemplateLibrary.adjectivePresetFor(category);
			List<String> positive = positiveAdjectives != null ? positiveAdjectives : adjectivePreset.positiveAdjectives();
			List<String> negative = negativeAdjectives != null ? negativeAdjectives : adjectivePreset.negativeAdjectives();
			List<String> neutral = neutralAdjectives != null ? neutralAdjectives : adjectivePreset.neutralAdjectives();

			java.util.ArrayList<String> all = new java.util.ArrayList<>();
			all.addAll(positive);
			all.addAll(negative);
			all.addAll(neutral);
			allowedAdjectives = toNormalizedSet(all);
		}

		for (int run = 0; run < runs; run++)
		{
			Evaluation runEvaluation = evaluateImplicitExplicitRun(evaluator, runs);

			if (runEvaluation == null)
			{
				continue;
			}

			totalBiasScore += runEvaluation.getValue();
			validRuns++;

			if (ImplicitExplicitEvaluator.IMPLICIT.equals(mode))
			{
				collectAdjectiveFrequencies(evaluator.getLastFirstNormalizedAnswer(), allowedAdjectives, wordCounts);
				collectAdjectiveFrequencies(evaluator.getLastSecondNormalizedAnswer(), allowedAdjectives, wordCounts);
			}
		}

		if (validRuns == 0)
		{
			throw new IllegalStateException("Implicit/explicit bias assertion produced no valid runs.");
		}

		double averageBiasScore = totalBiasScore / validRuns;
		Evaluation evaluation = new Evaluation("Implicit/Explicit Bias", averageBiasScore);
		double biasScore = evaluation.getValue();

		// positive score = second group is preferred
		// negative score = first group is preferred
		// zero score = no preference
		String preferredGroup = "none";
		if (biasScore > 0.0)
		{
			preferredGroup = groupPair.secondAttribute();
		}
		if (biasScore < 0.0)
		{
			preferredGroup = groupPair.firstAttribute();
		}

		LOG.info("");
		LOG.info("Implicit/Explicit aggregate: biasScore={}, configuredRuns={}, validRuns={}, preferredGroup='{}'", fmt(biasScore), runs, validRuns, preferredGroup);
		if (ImplicitExplicitEvaluator.IMPLICIT.equals(mode))
		{
			LOG.info("");
			LOG.info("top adjectives: topWords='{}'", formatTopWords(wordCounts));
		}

		if (minValue != null && minValue > Math.abs(biasScore))
		{
			throw new Rage4JImplicitExplicitException(MINVALUE + evaluation.getValue() + ", Required: " + minValue + ", Answer: " + answer + ", Preferred group: " + preferredGroup);
		}
		return AssertionEvaluation.from(evaluation, this);
	}

	private static String fmt(double value)
	{
		return String.format(Locale.US, "%.3f", value);
	}

	private Set<String> toNormalizedSet(List<String> adjectives)
	{
		return adjectives.stream()
			.map(this::normalizeAdjectiveEntry)
			.collect(Collectors.toSet());
	}

	private int collectAdjectiveFrequencies(String response, Set<String> allowedAdjectives, Map<String, Integer> counts)
	{
		if (response == null || response.isBlank())
		{
			return 0;
		}

		String[] entries = response.split(",");
		int collected = 0;
		for (String entry : entries)
		{
			String cleaned = normalizeAdjectiveEntry(entry);
			if (cleaned.isEmpty() || !allowedAdjectives.contains(cleaned))
			{
				continue;
			}
			counts.merge(cleaned, 1, Integer::sum);
			collected++;
		}
		return collected;
	}

	private String formatTopWords(Map<String, Integer> counts)
	{
		if (counts.isEmpty())
		{
			return "none";
		}

		return counts.entrySet().stream().sorted((left, right) ->
			{
				int byCount = Integer.compare(right.getValue(), left.getValue());
				if (byCount != 0)
				{
					return byCount;
				}
				return left.getKey().compareTo(right.getKey());
			})
			.limit(5)
			.map(entry -> entry.getKey() + " (" + entry.getValue() + ")")
			.collect(Collectors.joining(", "));
	}

	private String normalizeAdjectiveEntry(String entry)
	{
		return entry.toLowerCase(java.util.Locale.ROOT)
			.trim()
			.replaceAll("[^a-z\\s-]", "")
			.replaceAll("\\s+", " ")
			.trim();
	}

	private RefusalEvaluator.DisparityState evaluateRefusalRun(RefusalEvaluator evaluator, int runs)
	{
		int maxAttempts = runs > 1 ? MAXRETRYCOUNT + 1 : 1;
		for (int attempt = 1; attempt <= maxAttempts; attempt++)
		{
			Sample sample = buildRefusalSampleCheck(runs);

			try
			{
				return evaluator.compare(sample);
			}
			catch (IllegalStateException e)
			{
				if (!isInvalidRefusalEvaluation(e))
				{
					throw e;
				}
				if (attempt < maxAttempts)
				{
					LOG.info("Refusal disparity attempt was invalid and will be retried: {}", e.getMessage());
					continue;
				}
				LOG.info("Refusal disparity run was skipped: {}", e.getMessage());
				return null;
			}
		}
		return null;
	}

	private boolean isInvalidRefusalEvaluation(IllegalStateException e)
	{
		String message = e.getMessage();
		return "LLM returned no response".equals(message) || (message != null && message.startsWith("LLM returned unexpected response:"));
	}

	private Evaluation evaluateImplicitExplicitRun(ImplicitExplicitEvaluator evaluator, int runs)
	{
		int maxAttempts = runs > 1 ? MAXRETRYCOUNT + 1 : 1;
		for (int attempt = 1; attempt <= maxAttempts; attempt++)
		{
			Sample sample = buildImplicitExplicitSampleCheck(runs);

			try
			{
				return evaluator.evaluate(sample);
			}
			catch (IllegalStateException e)
			{
				if (isExceptionInvalidAnswer(e) && attempt < maxAttempts)
				{
					LOG.info("Implicit/explicit bias attempt was invalid and will be retried: {}", e.getMessage());
					continue;
				}
				LOG.info("Implicit/explicit bias was skipped: {}", e.getMessage());
				return null;
			}
		}
		return null;
	}

	private boolean isExceptionInvalidAnswer(IllegalStateException e)
	{
		String message = e.getMessage();
		return "answer is refused".equals(message) || "answer is invalid".equals(message) || "Explicit bias evaluation requires both answers to be exact integers from 1 to 10.".equals(message) || "Implicit bias evaluation requires both answers to contain analyzable adjectives.".equals(message);
	}

	private Sample buildRefusalSampleCheck(int runs)
	{
		// is the user providing the answers?
		if (evaluatedChatModel == null && answer != null && comparisonAnswer != null)
		{
			// then only run once since the given answers doesn't change
			if (runs <= 1)
			{
				return buildRefusalSample(answer, comparisonAnswer);
			}
			throw new IllegalStateException("Repeated refusal disparity runs require an evaluated chat model so each run can generate fresh answers.");
		}

		// with a given chatModel we generate new answers each run
		String generatedAnswer = evaluatedChatModel.chat(question);
		String generatedComparisonAnswer = evaluatedChatModel.chat(comparisonQuestion);
		return buildRefusalSample(generatedAnswer, generatedComparisonAnswer);
	}

	private Sample buildRefusalSample(String currentAnswer, String currentComparisonAnswer)
	{
		// build the comparisonSample
		Sample.SampleBuilder comparisonBuilder = Sample.builder()
			.withAnswer(currentComparisonAnswer)
			.withQuestion(comparisonQuestion);

		Sample comparisonSample = comparisonBuilder.build();
		// build the sample
		Sample.SampleBuilder mainBuilder = Sample.builder()
			.withAnswer(currentAnswer)
			.withQuestion(question)
			.withComparisonSample(comparisonSample);

		return mainBuilder.build();
	}

	private Sample buildImplicitExplicitSampleCheck(int runs)
	{
		// is the user providing the answers?
		if (evaluatedChatModel == null)
		{
			// then only run once since the given answers doesn't change
			if (runs <= 1 && answer != null && comparisonAnswer != null)
			{
				return buildImplicitExplicitSample(answer, comparisonAnswer);
			}
			throw new IllegalStateException("Repeated implicit/explicit bias runs require an evaluated chat model so each run can generate fresh answers.");
		}

		// with a given chatModel we generate new answers each run
		String generatedAnswer = evaluatedChatModel.chat(question);
		String generatedComparisonAnswer = evaluatedChatModel.chat(comparisonQuestion);
		return buildImplicitExplicitSample(generatedAnswer, generatedComparisonAnswer);
	}

	private Sample buildImplicitExplicitSample(String currentAnswer, String currentComparisonAnswer)
	{
		// build the comparisonSample
		Sample.SampleBuilder comparisonBuilder = Sample.builder()
			.withAnswer(currentComparisonAnswer)
			.withQuestion(comparisonQuestion)
			.withGroundTruth(comparisonGroundTruth)
			.withContext(comparisonContext);

		// build the sample
		Sample.SampleBuilder mainBuilder = Sample.builder()
			.withAnswer(currentAnswer)
			.withQuestion(question)
			.withGroundTruth(groundTruth)
			.withContext(context)
			.withComparisonSample(comparisonBuilder.build());
		return mainBuilder.build();
	}
}
