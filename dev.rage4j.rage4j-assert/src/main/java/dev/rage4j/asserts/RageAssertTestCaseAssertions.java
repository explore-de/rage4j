package dev.rage4j.asserts;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.rage4j.asserts.exception.Rage4JBleuScoreException;
import dev.rage4j.asserts.exception.Rage4JCorrectnessException;
import dev.rage4j.asserts.exception.Rage4JFaithfulnessException;
import dev.rage4j.asserts.exception.Rage4JFalsePositiveRateDisparityException;
import dev.rage4j.asserts.exception.Rage4JImplicitExplicitBiasException;
import dev.rage4j.asserts.exception.Rage4JLocalGenderBiasException;
import dev.rage4j.asserts.exception.Rage4JRefusalException;
import dev.rage4j.asserts.exception.Rage4JRelevanceException;
import dev.rage4j.asserts.exception.Rage4JRougeScoreException;
import dev.rage4j.asserts.exception.Rage4JSimilarityException;
import dev.rage4j.evaluation.Evaluation;
import dev.rage4j.evaluation.answercorrectness.AnswerCorrectnessEvaluator;
import dev.rage4j.evaluation.answerrelevance.AnswerRelevanceEvaluator;
import dev.rage4j.evaluation.answersemanticsimilarity.AnswerSemanticSimilarityEvaluator;
import dev.rage4j.evaluation.bleuscore.BleuScoreEvaluator;
import dev.rage4j.evaluation.bias.FalsePositiveRateDisparity.FalsePositiveRateDisparityEvaluator;
import dev.rage4j.evaluation.bias.ImplicitExplicitBias.ImplicitExplicitBiasEvaluator;
import dev.rage4j.evaluation.bias.ImplicitExplicitBias.support.ImplicitExplicitBiasTemplateLibrary;
import dev.rage4j.evaluation.bias.ImplicitExplicitBias.support.ImplicitExplicitBiasTemplateLibrary.AdjectivePreset;
import dev.rage4j.evaluation.bias.ImplicitExplicitBias.support.ImplicitExplicitBiasTemplateLibrary.ConfiguredGroupPair;
import dev.rage4j.evaluation.bias.ImplicitExplicitBias.support.ImplicitExplicitBiasTemplateLibrary.GroupPair;
import dev.rage4j.evaluation.bias.Refusal.RefusalEvaluator;
import dev.rage4j.evaluation.faithfulness.FaithfulnessEvaluator;
import dev.rage4j.evaluation.rougescore.RougeScoreEvaluator;
import dev.rage4j.model.Sample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class RageAssertTestCaseAssertions
{
	private static final Logger LOG = LoggerFactory.getLogger(RageAssertTestCaseAssertions.class);
	private static final String MINVALUE = "Answer did not reach required min value! Evaluated value: ";
	private static final String MAXVALUE = "Answer exceeded allowed max value! Evaluated value: ";
	private static final String ABSOLUTE_MAXVALUE = "Answer exceeded allowed max absolute value! Evaluated value: ";
	private static final int DEFAULT_RUNS_PER_SCENARIO = 10;

	private final EmbeddingModel embeddingModel;
	private final String question;
	private final String groundTruth;
	private final List<String> contextList;
	private final String answer;
	private final String comparisonQuestion;
	private final String comparisonGroundTruth;
	private final List<String> comparisonContextList;
	private final String comparisonAnswer;
	private final ImplicitExplicitScenario implicitExplicitScenario;
	private final ChatModel judgeChatModel;

	public RageAssertTestCaseAssertions(
		String answer,
		String groundTruth,
		String question,
		List<String> contextList,
		String comparisonAnswer,
		String comparisonGroundTruth,
		String comparisonQuestion,
		List<String> comparisonContextList,
		ImplicitExplicitScenario implicitExplicitScenario,
		ChatModel judgeChatModel,
		EmbeddingModel embeddingModel)
	{
		this.answer = answer;
		this.groundTruth = groundTruth;
		this.question = question;
		this.contextList = contextList;
		this.comparisonAnswer = comparisonAnswer;
		this.comparisonGroundTruth = comparisonGroundTruth;
		this.comparisonQuestion = comparisonQuestion;
		this.comparisonContextList = comparisonContextList;
		this.implicitExplicitScenario = implicitExplicitScenario;
		this.judgeChatModel = judgeChatModel;
		this.embeddingModel = embeddingModel;
	}

	public AssertionEvaluation assertFaithfulness(double minValue)
	{
		FaithfulnessEvaluator evaluator = new FaithfulnessEvaluator(
			requireJudgeChatModel("Faithfulness assertion requires a judge chat model."));
		Sample sample = Sample.builder()
			.withAnswer(answer)
			.withGroundTruth(groundTruth)
			.withQuestion(question)
			.withContextsList(contextList)
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
		AnswerCorrectnessEvaluator evaluator = new AnswerCorrectnessEvaluator(
			requireJudgeChatModel("Answer correctness assertion requires a judge chat model."));
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
		AnswerRelevanceEvaluator evaluator = new AnswerRelevanceEvaluator(
			requireJudgeChatModel("Answer relevance assertion requires a judge chat model."),
			embeddingModel);
		Sample sample = Sample.builder()
			.withAnswer(answer)
			.withQuestion(question)
			.withContextsList(contextList)
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

	public AssertionEvaluation assertRefusalDisparity(double maxValue)
	{
		return assertRefusalDisparity(maxValue, 1);
	}

	public AssertionEvaluation assertRefusalDisparity(int runs)
	{
		return assertRefusalDisparity(0.0, runs);
	}

	public AssertionEvaluation assertRefusalDisparity(double maxValue, int runs)
	{
		if (runs <= 0)
		{
			throw new IllegalArgumentException("runs must be greater than 0");
		}

		ChatModel judgeModel = requireJudgeChatModel("Refusal disparity assertion requires a judge chat model.");
		RefusalEvaluator evaluator = new RefusalEvaluator(judgeModel);
		double totalScore = 0.0;
		int unfairRuns = 0;
		int firstRefusals = 0;
		int secondRefusals = 0;

		for (int run = 0; run < runs; run++)
		{
			Sample sample = buildPairedSample();
			RefusalEvaluator.DisparityState comparison = evaluator.compare(sample);
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
				totalScore += 1.0;
				unfairRuns++;
			}
		}

		double averageScore = totalScore / runs;
		Evaluation evaluation = new Evaluation("Refusal Disparity", averageScore);
		boolean hadAnyUnfairRun = unfairRuns > 0;
		LOG.info("Refusal disparity aggregate: unfairRuns={}, configuredRuns={}, allFair={}, hadAnyUnfairRun={}, firstRefusals={}, secondRefusals={}, averageScore={}",
			unfairRuns, runs, !hadAnyUnfairRun, hadAnyUnfairRun, firstRefusals, secondRefusals, averageScore);

		if (evaluation.getValue() > maxValue)
		{
			throw new Rage4JRefusalException(MAXVALUE + evaluation.getValue() + ", Allowed: " + maxValue);
		}
		return AssertionEvaluation.from(evaluation, this);
	}

	public AssertionEvaluation assertFalsePositiveRateDisparity(double maxValue)
	{
		ChatModel judgeModel = requireJudgeChatModel("False Positive Rate Disparity assertion requires a judge chat model.");
		FalsePositiveRateDisparityEvaluator evaluator = new FalsePositiveRateDisparityEvaluator(judgeModel);
		Sample sample = buildPairedSample();
		Evaluation evaluation = evaluator.evaluate(sample);

		if (evaluation.getValue() > maxValue)
		{
			throw new Rage4JFalsePositiveRateDisparityException(
				MAXVALUE + evaluation.getValue() + ", Allowed: " + maxValue);
		}
		return AssertionEvaluation.from(evaluation, this);
	}

	public AssertionEvaluation assertImplicitExplicitBias(String category, String mode, double maxBiasScore)
	{
		return assertImplicitExplicitBias(category, mode, maxBiasScore, DEFAULT_RUNS_PER_SCENARIO);
	}

	public AssertionEvaluation assertImplicitExplicitBias(double maxBiasScore)
	{
		return assertImplicitExplicitBias(maxBiasScore, DEFAULT_RUNS_PER_SCENARIO);
	}

	public AssertionEvaluation assertImplicitExplicitBias(double maxBiasScore, int runs)
	{
		if (implicitExplicitScenario == null)
		{
			throw new IllegalStateException("An implicitExplicitScenario must be set before assertImplicitExplicitBias(maxBiasScore, runs) is used.");
		}

		AdjectivePreset adjectivePreset = implicitExplicitScenario.adjectivePreset();
		if (adjectivePreset != null)
		{
			return assertImplicitExplicitBias(
				implicitExplicitScenario.effectiveCategory(),
				implicitExplicitScenario.mode().value(),
				maxBiasScore,
				runs,
				implicitExplicitScenario.groupPair(),
				adjectivePreset.positiveAdjectives(),
				adjectivePreset.negativeAdjectives(),
				adjectivePreset.neutralAdjectives());
		}

		return assertImplicitExplicitBias(
			implicitExplicitScenario.effectiveCategory(),
			implicitExplicitScenario.mode().value(),
			maxBiasScore,
			runs,
			implicitExplicitScenario.groupPair());
	}

	public AssertionEvaluation assertImplicitExplicitBias(GroupPair groupPair, String mode, double maxBiasScore)
	{
		return assertImplicitExplicitBias(groupPair, mode, maxBiasScore, DEFAULT_RUNS_PER_SCENARIO);
	}

	public AssertionEvaluation assertImplicitExplicitBias(String category, String mode, double maxBiasScore, GroupPair groupPair)
	{
		return assertImplicitExplicitBias(category, mode, maxBiasScore, DEFAULT_RUNS_PER_SCENARIO, groupPair);
	}

	public AssertionEvaluation assertImplicitExplicitBias(GroupPair groupPair, String mode, double maxBiasScore, int runs)
	{
		return assertImplicitExplicitBias("CUSTOM", mode, maxBiasScore, runs, groupPair);
	}

	public AssertionEvaluation assertImplicitExplicitBias(GroupPair groupPair, String mode, double maxBiasScore, int runs,
		List<String> positiveAdjectives, List<String> negativeAdjectives, List<String> neutralAdjectives)
	{
		return assertImplicitExplicitBias("CUSTOM", mode, maxBiasScore, runs, groupPair,
			positiveAdjectives, negativeAdjectives, neutralAdjectives);
	}

	public AssertionEvaluation assertImplicitExplicitBias(ConfiguredGroupPair configuredGroupPair, String mode, double maxBiasScore)
	{
		return assertImplicitExplicitBias(configuredGroupPair, mode, maxBiasScore, DEFAULT_RUNS_PER_SCENARIO);
	}

	public AssertionEvaluation assertImplicitExplicitBias(ConfiguredGroupPair configuredGroupPair, String mode, double maxBiasScore, int runs)
	{
		String category = configuredGroupPair.adjectiveCategory();
		if (category == null || category.isBlank())
		{
			category = ImplicitExplicitBiasTemplateLibrary.CUSTOM;
		}

		AdjectivePreset adjectivePreset = configuredGroupPair.adjectivePreset();
		if (adjectivePreset != null)
		{
			return assertImplicitExplicitBias(
				category,
				mode,
				maxBiasScore,
				runs,
				configuredGroupPair.groupPair(),
				adjectivePreset.positiveAdjectives(),
				adjectivePreset.negativeAdjectives(),
				adjectivePreset.neutralAdjectives());
		}

		return assertImplicitExplicitBias(category, mode, maxBiasScore, runs, configuredGroupPair.groupPair());
	}

	public AssertionEvaluation assertImplicitExplicitBias(String category, String mode, double maxBiasScore, int runs,
		List<String> positiveAdjectives, List<String> negativeAdjectives, List<String> neutralAdjectives)
	{
		return assertImplicitExplicitBias(category, mode, maxBiasScore, runs, null,
			positiveAdjectives, negativeAdjectives, neutralAdjectives);
	}

	public AssertionEvaluation assertImplicitExplicitBias(String category, String mode, double maxBiasScore, int runs)
	{
		return assertImplicitExplicitBias(category, mode, maxBiasScore, runs, null);
	}

	public AssertionEvaluation assertImplicitExplicitBias(String category, String mode, double maxBiasScore, int runs, GroupPair groupPair)
	{
		return assertImplicitExplicitBias(category, mode, maxBiasScore, runs, groupPair, null, null, null);
	}

	public AssertionEvaluation assertImplicitExplicitBias(String category, String mode, double maxBiasScore, int runs,
		GroupPair groupPair, List<String> positiveAdjectives, List<String> negativeAdjectives, List<String> neutralAdjectives)
	{
		if (runs <= 0)
		{
			throw new IllegalArgumentException("runs must be greater than 0");
		}

		ChatModel normalizationModel =
			requireJudgeChatModel("Implicit/explicit bias assertion requires a judge chat model for normalization.");
		GroupPair resolvedGroupPair = resolveImplicitExplicitGroupPair(category, groupPair);
		Sample sample = buildImplicitExplicitBiasSample();
		ImplicitExplicitBiasEvaluator evaluator = new ImplicitExplicitBiasEvaluator(
			category,
			mode,
			resolvedGroupPair,
			normalizationModel,
			positiveAdjectives,
			negativeAdjectives,
			neutralAdjectives);

		double totalBiasScore = 0.0;
		int validRuns = 0;
		Map<String, Integer> firstWordCounts = new HashMap<>();
		Map<String, Integer> secondWordCounts = new HashMap<>();
		Set<String> allowedAdjectives = Set.of();
		Set<String> negativeAdjectiveSet = Set.of();
		Map<String, Integer> firstNegativeWordCounts = new HashMap<>();
		Map<String, Integer> secondNegativeWordCounts = new HashMap<>();
		int firstNegativeSelections = 0;
		int secondNegativeSelections = 0;
		if (ImplicitExplicitBiasEvaluator.IMPLICIT.equals(mode))
		{
			AdjectivePreset adjectivePreset = resolveAdjectivePreset(
				category, positiveAdjectives, negativeAdjectives, neutralAdjectives);
			allowedAdjectives = buildAllowedAdjectiveSet(adjectivePreset);
			negativeAdjectiveSet = toNormalizedSet(adjectivePreset.negativeAdjectives());
		}

		for (int run = 0; run < runs; run++)
		{
			try
			{
				Evaluation runEvaluation = evaluator.evaluate(sample);
				totalBiasScore += runEvaluation.getValue();
				validRuns++;
				LOG.info("Run bias score: {}", fmt(runEvaluation.getValue()));

				if (ImplicitExplicitBiasEvaluator.IMPLICIT.equals(mode))
				{
					String normalizedFirstAnswer = evaluator.getLastFirstNormalizedAnswer();
					String normalizedSecondAnswer = evaluator.getLastSecondNormalizedAnswer();
					collectAdjectiveFrequencies(normalizedFirstAnswer, allowedAdjectives, firstWordCounts);
					collectAdjectiveFrequencies(normalizedSecondAnswer, allowedAdjectives, secondWordCounts);
					int firstNegativeSelectionsThisRun = collectAdjectiveFrequencies(
						normalizedFirstAnswer,
						negativeAdjectiveSet,
						firstNegativeWordCounts);
					int secondNegativeSelectionsThisRun = collectAdjectiveFrequencies(
						normalizedSecondAnswer,
						negativeAdjectiveSet,
						secondNegativeWordCounts);
					firstNegativeSelections += firstNegativeSelectionsThisRun;
					secondNegativeSelections += secondNegativeSelectionsThisRun;
					logNegativeImplicitSelections(
						run + 1,
						normalizedFirstAnswer,
						normalizedSecondAnswer,
						negativeAdjectiveSet,
						firstNegativeSelectionsThisRun,
						secondNegativeSelectionsThisRun);
				}
			}
			catch (IllegalStateException e)
			{
				LOG.info("Implicit/explicit bias run {} was skipped: {}", run + 1, e.getMessage());
			}
		}

		if (validRuns == 0)
		{
			throw new IllegalStateException("Implicit/explicit bias assertion produced no valid runs.");
		}

		double averageBiasScore = totalBiasScore / validRuns;
		Evaluation evaluation = new Evaluation("Implicit/Explicit Bias", averageBiasScore);
		double biasScore = evaluation.getValue();
		String preferredGroup = determinePreferredGroup(
			biasScore,
			resolvedGroupPair.firstGroup(),
			resolvedGroupPair.secondGroup());

		LOG.info("Implicit/explicit bias aggregate [{}]: mode={}, firstGroup='{}', secondGroup='{}', biasScore={}, validRuns={}, configuredRuns={}, preferredGroup='{}'",
			category, mode, resolvedGroupPair.firstGroup(),
			resolvedGroupPair.secondGroup(), fmt(biasScore), validRuns, runs, preferredGroup);
		if (ImplicitExplicitBiasEvaluator.IMPLICIT.equals(mode))
		{
			LOG.info("Implicit/explicit bias top adjectives [{}]: firstTopWords='{}', secondTopWords='{}'",
				category, formatTopWords(firstWordCounts), formatTopWords(secondWordCounts));
			LOG.info("Implicit/explicit bias negative adjective summary [{}]: totalNegativeSelections={}, firstNegativeSelections={}, secondNegativeSelections={}, firstTopNegativeWords='{}', secondTopNegativeWords='{}'",
				category,
				firstNegativeSelections + secondNegativeSelections,
				firstNegativeSelections,
				secondNegativeSelections,
				formatTopWords(firstNegativeWordCounts),
				formatTopWords(secondNegativeWordCounts));
		}

		if (Math.abs(biasScore) > maxBiasScore)
		{
			String negativeSelectionsSummary = "";
			if (ImplicitExplicitBiasEvaluator.IMPLICIT.equals(mode))
			{
				negativeSelectionsSummary = ", Negative adjective selections over runs: total="
					+ (firstNegativeSelections + secondNegativeSelections)
					+ ", first=" + firstNegativeSelections
					+ ", second=" + secondNegativeSelections;
			}
			throw new Rage4JImplicitExplicitBiasException(
				ABSOLUTE_MAXVALUE + biasScore + ", Allowed: " + maxBiasScore + ", Preferred group: " + preferredGroup
					+ negativeSelectionsSummary);
		}
		return AssertionEvaluation.from(evaluation, this);
	}

	private void logNegativeImplicitSelections(int runNumber, String normalizedFirstAnswer, String normalizedSecondAnswer,
		Set<String> negativeAdjectives, int firstNegativeSelectionsThisRun, int secondNegativeSelectionsThisRun)
	{
		if (firstNegativeSelectionsThisRun == 0 && secondNegativeSelectionsThisRun == 0)
		{
			return;
		}

		LOG.info(
			"Implicit/explicit bias run {} selected negative adjectives: firstCount={}, secondCount={}, firstWords='{}', secondWords='{}'",
			runNumber,
			firstNegativeSelectionsThisRun,
			secondNegativeSelectionsThisRun,
			formatMatchingWords(normalizedFirstAnswer, negativeAdjectives),
			formatMatchingWords(normalizedSecondAnswer, negativeAdjectives));
	}

	private GroupPair resolveImplicitExplicitGroupPair(String category, GroupPair groupPair)
	{
		if (groupPair != null)
		{
			return groupPair;
		}
		if (category == null || category.isBlank()
			|| ImplicitExplicitBiasTemplateLibrary.CUSTOM.equalsIgnoreCase(category))
		{
			throw new IllegalStateException("A groupPair is required for CUSTOM implicit/explicit bias assertions.");
		}
		return ImplicitExplicitBiasTemplateLibrary.presetFor(category).primaryGroupPair();
	}

	private AdjectivePreset resolveAdjectivePreset(String category, List<String> positiveAdjectives, List<String> negativeAdjectives,
		List<String> neutralAdjectives)
	{
		AdjectivePreset adjectivePreset = ImplicitExplicitBiasTemplateLibrary.adjectivePresetFor(category);
		List<String> positive = positiveAdjectives != null ? positiveAdjectives : adjectivePreset.positiveAdjectives();
		List<String> negative = negativeAdjectives != null ? negativeAdjectives : adjectivePreset.negativeAdjectives();
		List<String> neutral = neutralAdjectives != null ? neutralAdjectives : adjectivePreset.neutralAdjectives();
		return new AdjectivePreset(positive, negative, neutral);
	}

	private Set<String> buildAllowedAdjectiveSet(AdjectivePreset adjectivePreset)
	{
		java.util.ArrayList<String> all = new java.util.ArrayList<>();
		all.addAll(adjectivePreset.positiveAdjectives());
		all.addAll(adjectivePreset.negativeAdjectives());
		all.addAll(adjectivePreset.neutralAdjectives());
		return toNormalizedSet(all);
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

	private String formatMatchingWords(String response, Set<String> allowedAdjectives)
	{
		if (response == null || response.isBlank())
		{
			return "none";
		}

		List<String> matches = new java.util.ArrayList<>();
		String[] entries = response.split(",");
		for (String entry : entries)
		{
			String cleaned = normalizeAdjectiveEntry(entry);
			if (!cleaned.isEmpty() && allowedAdjectives.contains(cleaned))
			{
				matches.add(cleaned);
			}
		}
		if (matches.isEmpty())
		{
			return "none";
		}
		return String.join(", ", matches);
	}

	private String formatTopWords(Map<String, Integer> counts)
	{
		if (counts.isEmpty())
		{
			return "none";
		}

		return counts.entrySet().stream()
			.sorted((left, right) ->
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
		return entry.toLowerCase(Locale.ROOT)
			.trim()
			.replaceAll("[^a-z\\s-]", "")
			.replaceAll("\\s+", " ")
			.trim();
	}

	private String determinePreferredGroup(double biasScore, String firstGroup, String secondGroup)
	{
		if (biasScore > 0.0)
		{
			return secondGroup;
		}
		if (biasScore < 0.0)
		{
			return firstGroup;
		}
		return "none";
	}

	private String fmt(double value)
	{
		return String.format(Locale.US, "%.3f", value);
	}

	private ChatModel requireJudgeChatModel(String message)
	{
		if (judgeChatModel == null)
		{
			throw new IllegalStateException(message);
		}
		return judgeChatModel;
	}

	private Sample buildSingleSample()
	{
		Sample.SampleBuilder builder = Sample.builder()
			.withAnswer(answer)
			.withQuestion(question);

		if (groundTruth != null)
		{
			builder.withGroundTruth(groundTruth);
		}
		if (contextList != null)
		{
			builder.withContextsList(contextList);
		}
		return builder.build();
	}

	private Sample buildPairedSample()
	{
		requireComparisonSample();

		Sample.SampleBuilder comparisonBuilder = Sample.builder()
			.withAnswer(comparisonAnswer)
			.withQuestion(comparisonQuestion);
		if (comparisonGroundTruth != null)
		{
			comparisonBuilder.withGroundTruth(comparisonGroundTruth);
		}
		if (comparisonContextList != null)
		{
			comparisonBuilder.withContextsList(comparisonContextList);
		}

		Sample comparisonSample = comparisonBuilder.build();
		Sample.SampleBuilder mainBuilder = Sample.builder()
			.withAnswer(answer)
			.withQuestion(question)
			.withComparisonSample(comparisonSample);
		if (groundTruth != null)
		{
			mainBuilder.withGroundTruth(groundTruth);
		}
		if (contextList != null)
		{
			mainBuilder.withContextsList(contextList);
		}
		return mainBuilder.build();
	}

	private Sample buildImplicitExplicitBiasSample()
	{
		if (comparisonAnswer == null)
		{
			throw new IllegalStateException("Comparison answer is required for paired bias assertions.");
		}
		if (answer == null)
		{
			throw new IllegalStateException("Answer is required for paired bias assertions.");
		}

		Sample.SampleBuilder comparisonBuilder = Sample.builder()
			.withAnswer(comparisonAnswer);
		if (comparisonQuestion != null)
		{
			comparisonBuilder.withQuestion(comparisonQuestion);
		}
		if (comparisonGroundTruth != null)
		{
			comparisonBuilder.withGroundTruth(comparisonGroundTruth);
		}
		if (comparisonContextList != null)
		{
			comparisonBuilder.withContextsList(comparisonContextList);
		}

		Sample.SampleBuilder mainBuilder = Sample.builder()
			.withAnswer(answer)
			.withComparisonSample(comparisonBuilder.build());
		if (question != null)
		{
			mainBuilder.withQuestion(question);
		}
		if (groundTruth != null)
		{
			mainBuilder.withGroundTruth(groundTruth);
		}
		if (contextList != null)
		{
			mainBuilder.withContextsList(contextList);
		}
		return mainBuilder.build();
	}

	private void requireComparisonSample()
	{
		if (comparisonQuestion == null || comparisonQuestion.trim().isEmpty())
		{
			throw new IllegalStateException("Comparison question is required for paired bias assertions.");
		}
		if (comparisonAnswer == null)
		{
			throw new IllegalStateException("Comparison answer is required for paired bias assertions.");
		}
		if (answer == null)
		{
			throw new IllegalStateException("Answer is required for paired bias assertions.");
		}
	}
}
