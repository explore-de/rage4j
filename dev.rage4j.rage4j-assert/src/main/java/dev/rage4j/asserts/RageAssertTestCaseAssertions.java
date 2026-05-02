package dev.rage4j.asserts;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.rage4j.asserts.exception.Rage4JBleuScoreException;
import dev.rage4j.asserts.exception.Rage4JCorrectnessException;
import dev.rage4j.asserts.exception.Rage4JFaithfulnessException;
import dev.rage4j.asserts.exception.Rage4JImplicitExplicitBiasException;
import dev.rage4j.asserts.exception.Rage4JRefusalException;
import dev.rage4j.asserts.exception.Rage4JRelevanceException;
import dev.rage4j.asserts.exception.Rage4JRougeScoreException;
import dev.rage4j.asserts.exception.Rage4JSimilarityException;
import dev.rage4j.evaluation.Evaluation;
import dev.rage4j.evaluation.answercorrectness.AnswerCorrectnessEvaluator;
import dev.rage4j.evaluation.answerrelevance.AnswerRelevanceEvaluator;
import dev.rage4j.evaluation.answersemanticsimilarity.AnswerSemanticSimilarityEvaluator;
import dev.rage4j.evaluation.bleuscore.BleuScoreEvaluator;
import dev.rage4j.evaluation.bias.implicitexplicit.ImplicitExplicitBiasEvaluator;
import dev.rage4j.evaluation.bias.implicitexplicit.support.ImplicitExplicitBiasTemplateLibrary;
import dev.rage4j.evaluation.bias.implicitexplicit.support.ImplicitExplicitBiasTemplateLibrary.AdjectivePreset;
import dev.rage4j.evaluation.bias.implicitexplicit.support.ImplicitExplicitBiasTemplateLibrary.ConfiguredGroupPair;
import dev.rage4j.evaluation.bias.implicitexplicit.support.ImplicitExplicitBiasTemplateLibrary.GroupPair;
import dev.rage4j.evaluation.bias.refusal.RefusalEvaluator;
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
	private static final double DEFAULT_MIN_SCORE = 0.0;
	private static final int RETRIES_PER_INVALID_RUN = 3;

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
	private final ChatModel evaluatedChatModel;

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
		ChatModel evaluatedChatModel,
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
		this.evaluatedChatModel = evaluatedChatModel;
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

	public AssertionEvaluation assertFaithfulness()
	{
		return assertFaithfulness(DEFAULT_MIN_SCORE);
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

	public AssertionEvaluation assertAnswerCorrectness()
	{
		return assertAnswerCorrectness(DEFAULT_MIN_SCORE);
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

	public AssertionEvaluation assertAnswerRelevance()
	{
		return assertAnswerRelevance(DEFAULT_MIN_SCORE);
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

	public AssertionEvaluation assertSemanticSimilarity()
	{
		return assertSemanticSimilarity(DEFAULT_MIN_SCORE);
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

	public AssertionEvaluation assertBleuScore()
	{
		return assertBleuScore(DEFAULT_MIN_SCORE);
	}

	public AssertionEvaluation assertRougeScore()
	{
		return assertRougeScore(
			DEFAULT_MIN_SCORE,
			RougeScoreEvaluator.RougeType.ROUGE1,
			RougeScoreEvaluator.MeasureType.F1SCORE);
	}

	public AssertionEvaluation assertRougeScore(RougeScoreEvaluator.RougeType rougeType, RougeScoreEvaluator.MeasureType measureType)
	{
		return assertRougeScore(DEFAULT_MIN_SCORE, rougeType, measureType);
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

	public AssertionEvaluation assertRefusalDisparity(double maxValue)
	{
		return assertRefusalDisparity(maxValue, 1);
	}

	public AssertionEvaluation assertRefusalDisparity(int runs)
	{
		return assertRefusalDisparity(null, runs);
	}

	public AssertionEvaluation assertRefusalDisparity(double maxValue, int runs)
	{
		return assertRefusalDisparity(Double.valueOf(maxValue), runs);
	}

	private AssertionEvaluation assertRefusalDisparity(Double maxValue, int runs)
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
			Sample sample = buildRefusalSampleForRun(runs, run + 1);
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
		LOG.info("");
		LOG.info("Refusal disparity aggregate: unfairRuns={}, configuredRuns={}, allFair={}, hadAnyUnfairRun={}, firstRefusals={}, secondRefusals={}, averageScore={}",
			unfairRuns, runs, !hadAnyUnfairRun, hadAnyUnfairRun, firstRefusals, secondRefusals, averageScore);

		if (maxValue != null && evaluation.getValue() > maxValue)
		{
			throw new Rage4JRefusalException(MAXVALUE + evaluation.getValue() + ", Allowed: " + maxValue);
		}
		return AssertionEvaluation.from(evaluation, this);
	}

	public AssertionEvaluation assertImplicitExplicitBias(String category, String mode, double maxBiasScore)
	{
		return assertImplicitExplicitBiasInternal(category, mode, maxBiasScore, 1, null, null, null, null);
	}

	public AssertionEvaluation assertImplicitExplicitBias(String category, String mode)
	{
		return assertImplicitExplicitBiasInternal(category, mode, null, 1, null, null, null, null);
	}

	public AssertionEvaluation assertImplicitExplicitBias(String category, String mode, int runs)
	{
		return assertImplicitExplicitBiasInternal(category, mode, null, runs, null, null, null, null);
	}

	public AssertionEvaluation assertImplicitExplicitBias()
	{
		return assertImplicitExplicitBias(null, 1);
	}

	public AssertionEvaluation assertImplicitExplicitBias(double maxBiasScore)
	{
		return assertImplicitExplicitBias(Double.valueOf(maxBiasScore), 1);
	}

	public AssertionEvaluation assertImplicitExplicitBias(int runs)
	{
		return assertImplicitExplicitBias(null, runs);
	}

	public AssertionEvaluation assertImplicitExplicitBias(double maxBiasScore, int runs)
	{
		return assertImplicitExplicitBias(Double.valueOf(maxBiasScore), runs);
	}

	private AssertionEvaluation assertImplicitExplicitBias(Double maxBiasScore, int runs)
	{
		if (implicitExplicitScenario == null)
		{
			throw new IllegalStateException("An implicitExplicitScenario must be set before assertImplicitExplicitBias is used.");
		}

		AdjectivePreset adjectivePreset = implicitExplicitScenario.adjectivePreset();
		if (adjectivePreset != null)
		{
			return assertImplicitExplicitBiasInternal(
				implicitExplicitScenario.effectiveCategory(),
				implicitExplicitScenario.mode().value(),
				maxBiasScore,
				runs,
				implicitExplicitScenario.groupPair(),
				adjectivePreset.positiveAdjectives(),
				adjectivePreset.negativeAdjectives(),
				adjectivePreset.neutralAdjectives());
		}

		return assertImplicitExplicitBiasInternal(
			implicitExplicitScenario.effectiveCategory(),
			implicitExplicitScenario.mode().value(),
			maxBiasScore,
			runs,
			implicitExplicitScenario.groupPair(),
			null,
			null,
			null);
	}

	public AssertionEvaluation assertImplicitExplicitBias(GroupPair groupPair, String mode, double maxBiasScore)
	{
		return assertImplicitExplicitBias("CUSTOM", mode, maxBiasScore, 1, groupPair);
	}

	public AssertionEvaluation assertImplicitExplicitBias(GroupPair groupPair, String mode)
	{
		return assertImplicitExplicitBiasInternal("CUSTOM", mode, null, 1, groupPair, null, null, null);
	}

	public AssertionEvaluation assertImplicitExplicitBias(String category, String mode, double maxBiasScore, GroupPair groupPair)
	{
		return assertImplicitExplicitBiasInternal(category, mode, maxBiasScore, 1, groupPair, null, null, null);
	}

	public AssertionEvaluation assertImplicitExplicitBias(String category, String mode, GroupPair groupPair)
	{
		return assertImplicitExplicitBiasInternal(category, mode, null, 1, groupPair, null, null, null);
	}

	public AssertionEvaluation assertImplicitExplicitBias(GroupPair groupPair, String mode, double maxBiasScore, int runs)
	{
		return assertImplicitExplicitBias("CUSTOM", mode, maxBiasScore, runs, groupPair);
	}

	public AssertionEvaluation assertImplicitExplicitBias(GroupPair groupPair, String mode, int runs)
	{
		return assertImplicitExplicitBiasInternal("CUSTOM", mode, null, runs, groupPair, null, null, null);
	}

	public AssertionEvaluation assertImplicitExplicitBias(GroupPair groupPair, String mode,
		List<String> positiveAdjectives, List<String> negativeAdjectives, List<String> neutralAdjectives)
	{
		return assertImplicitExplicitBiasInternal("CUSTOM", mode, null,
			1, groupPair, positiveAdjectives, negativeAdjectives, neutralAdjectives);
	}

	public AssertionEvaluation assertImplicitExplicitBias(GroupPair groupPair, String mode, int runs,
		List<String> positiveAdjectives, List<String> negativeAdjectives, List<String> neutralAdjectives)
	{
		return assertImplicitExplicitBiasInternal("CUSTOM", mode, null, runs,
			groupPair, positiveAdjectives, negativeAdjectives, neutralAdjectives);
	}

	public AssertionEvaluation assertImplicitExplicitBias(GroupPair groupPair, String mode, double maxBiasScore, int runs,
		List<String> positiveAdjectives, List<String> negativeAdjectives, List<String> neutralAdjectives)
	{
		return assertImplicitExplicitBias("CUSTOM", mode, maxBiasScore, runs, groupPair,
			positiveAdjectives, negativeAdjectives, neutralAdjectives);
	}

	public AssertionEvaluation assertImplicitExplicitBias(ConfiguredGroupPair configuredGroupPair, String mode, double maxBiasScore)
	{
		return assertImplicitExplicitBias(configuredGroupPair, mode, maxBiasScore, 1);
	}

	public AssertionEvaluation assertImplicitExplicitBias(ConfiguredGroupPair configuredGroupPair, String mode)
	{
		return assertImplicitExplicitBias(configuredGroupPair, mode, null, 1);
	}

	public AssertionEvaluation assertImplicitExplicitBias(ConfiguredGroupPair configuredGroupPair, String mode, int runs)
	{
		return assertImplicitExplicitBias(configuredGroupPair, mode, null, runs);
	}

	public AssertionEvaluation assertImplicitExplicitBias(ConfiguredGroupPair configuredGroupPair, String mode, double maxBiasScore, int runs)
	{
		return assertImplicitExplicitBias(configuredGroupPair, mode, Double.valueOf(maxBiasScore), runs);
	}

	private AssertionEvaluation assertImplicitExplicitBias(ConfiguredGroupPair configuredGroupPair, String mode, Double maxBiasScore, int runs)
	{
		String category = configuredGroupPair.adjectiveCategory();
		if (category == null || category.isBlank())
		{
			category = ImplicitExplicitBiasTemplateLibrary.CUSTOM;
		}

		AdjectivePreset adjectivePreset = configuredGroupPair.adjectivePreset();
		if (adjectivePreset != null)
		{
			return assertImplicitExplicitBiasInternal(
				category,
				mode,
				maxBiasScore,
				runs,
				configuredGroupPair.groupPair(),
				adjectivePreset.positiveAdjectives(),
				adjectivePreset.negativeAdjectives(),
				adjectivePreset.neutralAdjectives());
		}

		return assertImplicitExplicitBiasInternal(category, mode, maxBiasScore, runs, configuredGroupPair.groupPair(), null, null, null);
	}

	public AssertionEvaluation assertImplicitExplicitBias(String category, String mode, double maxBiasScore, int runs,
		List<String> positiveAdjectives, List<String> negativeAdjectives, List<String> neutralAdjectives)
	{
		return assertImplicitExplicitBias(category, mode, maxBiasScore, runs, null,
			positiveAdjectives, negativeAdjectives, neutralAdjectives);
	}

	public AssertionEvaluation assertImplicitExplicitBias(String category, String mode, int runs,
		List<String> positiveAdjectives, List<String> negativeAdjectives, List<String> neutralAdjectives)
	{
		return assertImplicitExplicitBiasInternal(category, mode, null, runs,
			null, positiveAdjectives, negativeAdjectives, neutralAdjectives);
	}

	public AssertionEvaluation assertImplicitExplicitBias(String category, String mode,
		List<String> positiveAdjectives, List<String> negativeAdjectives, List<String> neutralAdjectives)
	{
		return assertImplicitExplicitBiasInternal(category, mode, null,
			1, null, positiveAdjectives, negativeAdjectives, neutralAdjectives);
	}

	public AssertionEvaluation assertImplicitExplicitBias(String category, String mode, double maxBiasScore, int runs)
	{
		return assertImplicitExplicitBias(category, mode, maxBiasScore, runs, null);
	}

	public AssertionEvaluation assertImplicitExplicitBias(String category, String mode, double maxBiasScore, int runs, GroupPair groupPair)
	{
		return assertImplicitExplicitBias(category, mode, maxBiasScore, runs, groupPair, null, null, null);
	}

	public AssertionEvaluation assertImplicitExplicitBias(String category, String mode, int runs, GroupPair groupPair)
	{
		return assertImplicitExplicitBiasInternal(category, mode, null, runs, groupPair, null, null, null);
	}

	public AssertionEvaluation assertImplicitExplicitBias(String category, String mode, double maxBiasScore, int runs,
		GroupPair groupPair, List<String> positiveAdjectives, List<String> negativeAdjectives, List<String> neutralAdjectives)
	{
		return assertImplicitExplicitBiasInternal(category, mode, maxBiasScore, runs, groupPair,
			positiveAdjectives, negativeAdjectives, neutralAdjectives);
	}

	private AssertionEvaluation assertImplicitExplicitBiasInternal(String category, String mode, Double maxBiasScore, int runs,
		GroupPair groupPair, List<String> positiveAdjectives, List<String> negativeAdjectives, List<String> neutralAdjectives)
	{
		if (runs <= 0)
		{
			throw new IllegalArgumentException("runs must be greater than 0");
		}

		ChatModel normalizationModel =
			requireJudgeChatModel("Implicit/explicit bias assertion requires a judge chat model for normalization.");
		GroupPair resolvedGroupPair = resolveImplicitExplicitGroupPair(category, groupPair);
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
			Evaluation runEvaluation = evaluateImplicitExplicitBiasRun(evaluator, runs, run + 1);
			if (runEvaluation == null)
			{
				continue;
			}

			totalBiasScore += runEvaluation.getValue();
			validRuns++;

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

		LOG.info("");
		LOG.info("Implicit/explicit bias aggregate [{}]: mode={}, firstGroup='{}', secondGroup='{}', biasScore={}, validRuns={}, configuredRuns={}, preferredGroup='{}'",
			category, mode, resolvedGroupPair.firstGroup(),
			resolvedGroupPair.secondGroup(), fmt(biasScore), validRuns, runs, preferredGroup);
		if (ImplicitExplicitBiasEvaluator.IMPLICIT.equals(mode))
		{
			LOG.info("");
			LOG.info("top adjectives [{}]: firstTopWords='{}', secondTopWords='{}'", category, formatTopWords(firstWordCounts), formatTopWords(secondWordCounts));
			LOG.info("negative adjective summary [{}]: totalNegativeSelections={}, firstNegativeSelections={}, secondNegativeSelections={}, firstTopNegativeWords='{}', secondTopNegativeWords='{}'",
				category,
				firstNegativeSelections + secondNegativeSelections,
				firstNegativeSelections,
				secondNegativeSelections,
				formatTopWords(firstNegativeWordCounts),
				formatTopWords(secondNegativeWordCounts));
		}

		if (maxBiasScore != null && Math.abs(biasScore) > maxBiasScore)
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

	private Evaluation evaluateImplicitExplicitBiasRun(ImplicitExplicitBiasEvaluator evaluator, int runs, int runNumber)
	{
		int maxAttempts = runs > 1 ? RETRIES_PER_INVALID_RUN + 1 : 1;
		for (int attempt = 1; attempt <= maxAttempts; attempt++)
		{
			Sample sample = buildImplicitExplicitBiasSampleForRun(runs, runNumber);
			try
			{
				return evaluator.evaluate(sample);
			}
			catch (IllegalStateException e)
			{
				if (isRetryableImplicitExplicitBiasFailure(e) && attempt < maxAttempts)
				{
					LOG.info("Implicit/explicit bias run {} attempt {} was invalid and will be retried: {}",
						runNumber, attempt, e.getMessage());
					continue;
				}
				LOG.info("Implicit/explicit bias run {} was skipped: {}", runNumber, e.getMessage());
				return null;
			}
		}
		return null;
	}

	private boolean isRetryableImplicitExplicitBiasFailure(IllegalStateException e)
	{
		String message = e.getMessage();
		return "answer is refused".equals(message)
			|| "answer is invalid".equals(message)
			|| "Explicit bias evaluation requires both answers to be exact integers from 1 to 10.".equals(message)
			|| "Implicit bias evaluation requires both answers to contain analyzable adjectives.".equals(message);
	}

	private Sample buildRefusalSampleForRun(int runs, int runNumber)
	{
		if (runs <= 1 && answer != null && comparisonAnswer != null)
		{
			return buildPairedSample();
		}
		if (evaluatedChatModel == null)
		{
			if (runs <= 1)
			{
				return buildPairedSample();
			}
			throw new IllegalStateException(
				"Repeated refusal disparity runs require an evaluated chat model so each run can generate fresh answers.");
		}

		requireRefusalGenerationInputs();
		String generatedAnswer = evaluatedChatModel.chat(question);
		String generatedComparisonAnswer = evaluatedChatModel.chat(comparisonQuestion);
		return buildPairedSample(generatedAnswer, generatedComparisonAnswer);
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
		return buildPairedSample(answer, comparisonAnswer);
	}

	private Sample buildPairedSample(String currentAnswer, String currentComparisonAnswer)
	{
		requireComparisonQuestion();
		requireAnswer("Answer is required for paired bias assertions.", currentAnswer);
		requireAnswer("Comparison answer is required for paired bias assertions.", currentComparisonAnswer);

		Sample.SampleBuilder comparisonBuilder = Sample.builder()
			.withAnswer(currentComparisonAnswer)
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
			.withAnswer(currentAnswer)
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
		return buildImplicitExplicitBiasSample(answer, comparisonAnswer);
	}

	private Sample buildImplicitExplicitBiasSampleForRun(int runs, int runNumber)
	{
		if (runs <= 1 && answer != null && comparisonAnswer != null)
		{
			return buildImplicitExplicitBiasSample();
		}
		if (evaluatedChatModel == null)
		{
			if (runs > 1)
			{
				throw new IllegalStateException(
					"Repeated implicit/explicit bias runs require an evaluated chat model so each run can generate fresh answers.");
			}
			return buildImplicitExplicitBiasSample();
		}

		requireImplicitExplicitGenerationInputs();
		String generatedAnswer = evaluatedChatModel.chat(question);
		String generatedComparisonAnswer = evaluatedChatModel.chat(comparisonQuestion);
		return buildImplicitExplicitBiasSample(generatedAnswer, generatedComparisonAnswer);
	}

	private Sample buildImplicitExplicitBiasSample(String currentAnswer, String currentComparisonAnswer)
	{
		requireAnswer("Comparison answer is required for paired bias assertions.", currentComparisonAnswer);
		requireAnswer("Answer is required for paired bias assertions.", currentAnswer);

		Sample.SampleBuilder comparisonBuilder = Sample.builder()
			.withAnswer(currentComparisonAnswer);
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
			.withAnswer(currentAnswer)
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
		requireComparisonQuestion();
		requireAnswer("Comparison answer is required for paired bias assertions.", comparisonAnswer);
		requireAnswer("Answer is required for paired bias assertions.", answer);
	}

	private void requireRefusalGenerationInputs()
	{
		if (question == null || question.trim().isEmpty())
		{
			throw new IllegalStateException("Question is required for repeated refusal disparity runs.");
		}
		requireComparisonQuestion();
	}

	private void requireImplicitExplicitGenerationInputs()
	{
		if (question == null || question.trim().isEmpty())
		{
			throw new IllegalStateException("Question is required for repeated implicit/explicit bias runs.");
		}
		requireComparisonQuestion();
	}

	private void requireComparisonQuestion()
	{
		if (comparisonQuestion == null || comparisonQuestion.trim().isEmpty())
		{
			throw new IllegalStateException("Comparison question is required for paired bias assertions.");
		}
	}

	private void requireAnswer(String message, String currentAnswer)
	{
		if (currentAnswer == null)
		{
			throw new IllegalStateException(message);
		}
	}
}
