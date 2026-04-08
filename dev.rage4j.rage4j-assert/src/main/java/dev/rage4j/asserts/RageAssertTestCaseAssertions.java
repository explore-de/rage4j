package dev.rage4j.asserts;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.rage4j.asserts.exception.Rage4JBleuScoreException;
import dev.rage4j.asserts.exception.Rage4JCorrectnessException;
import dev.rage4j.asserts.exception.Rage4JImplicitExplicitBiasException;
import dev.rage4j.asserts.exception.Rage4JFaithfulnessException;
import dev.rage4j.asserts.exception.Rage4JFalsePositiveRateDisparityException;
import dev.rage4j.asserts.exception.Rage4JLocalGenderBiasException;
import dev.rage4j.asserts.exception.Rage4JRelevanceException;
import dev.rage4j.asserts.exception.Rage4JRefusalException;
import dev.rage4j.asserts.exception.Rage4JRougeScoreException;
import dev.rage4j.asserts.exception.Rage4JSimilarityException;
import dev.rage4j.evaluation.Evaluation;
import dev.rage4j.evaluation.answercorrectness.AnswerCorrectnessEvaluator;
import dev.rage4j.evaluation.answerrelevance.AnswerRelevanceEvaluator;
import dev.rage4j.evaluation.answersemanticsimilarity.AnswerSemanticSimilarityEvaluator;
import dev.rage4j.evaluation.bleuscore.BleuScoreEvaluator;
import dev.rage4j.evaluation.bias.ImplicitExplicitBias.ImplicitExplicitBiasEvaluator;
import dev.rage4j.evaluation.bias.ImplicitExplicitBias.support.ImplicitExplicitBiasPromptBuilder;
import dev.rage4j.evaluation.bias.ImplicitExplicitBias.support.ImplicitExplicitBiasPromptBuilder.PromptPair;
import dev.rage4j.evaluation.bias.ImplicitExplicitBias.support.ImplicitExplicitBiasTemplateLibrary.AdjectivePreset;
import dev.rage4j.evaluation.bias.ImplicitExplicitBias.support.ImplicitExplicitBiasTemplateLibrary.ConfiguredGroupPair;
import dev.rage4j.evaluation.bias.ImplicitExplicitBias.support.ImplicitExplicitBiasTemplateLibrary;
import dev.rage4j.evaluation.bias.ImplicitExplicitBias.support.ImplicitExplicitBiasTemplateLibrary.GroupPair;
import dev.rage4j.evaluation.bias.FalsePositiveRateDisparity.FalsePositiveRateDisparityEvaluator;
import dev.rage4j.evaluation.bias.Refusal.RefusalEvaluator;
import dev.rage4j.evaluation.bias.LocalGender.LocalGenderBiasEvaluator;
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
	private ChatModel chatModel;
	private EmbeddingModel embeddingModel;
	private String question;
	private String groundTruth;
	private List<String> contextList;
	private String answer;
	private String comparisonQuestion;
	private String comparisonGroundTruth;
	private List<String> comparisonContextList;
	private String comparisonAnswer;
	private ChatModel judgeChatModel;

	private static final String MINVALUE = "Answer did not reach required min value! Evaluated value: ";
	private static final String MAXVALUE = "Answer exceeded allowed max value! Evaluated value: ";
	private static final String ABSOLUTE_MAXVALUE = "Answer exceeded allowed max absolute value! Evaluated value: ";
	private static final int DEFAULT_RUNS_PER_SCENARIO = 10;

	public RageAssertTestCaseAssertions(
		String answer,
		String groundTruth,
		String question,
		List<String> contextList,
		String comparisonAnswer,
		String comparisonGroundTruth,
		String comparisonQuestion,
		List<String> comparisonContextList,
		ChatModel chatModel,
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
		this.chatModel = chatModel;
		this.judgeChatModel = judgeChatModel;
		this.embeddingModel = embeddingModel;
	}

	public AssertionEvaluation assertFaithfulness(double minValue)
	{
		FaithfulnessEvaluator evaluator = new FaithfulnessEvaluator(chatModel);
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
		AnswerCorrectnessEvaluator evaluator = new AnswerCorrectnessEvaluator(chatModel);
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
		AnswerRelevanceEvaluator evaluator = new AnswerRelevanceEvaluator(chatModel, embeddingModel);
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

	public AssertionEvaluation assertRefusalDisparity(double maxValue, int runs)
	{
		if (runs <= 0)
		{
			throw new IllegalArgumentException("runs must be greater than 0");
		}

		ChatModel evaluatedModel = chatModel;
		ChatModel judgeModel = judgeChatModel != null
			? judgeChatModel
			: requireChatModel("Refusal disparity assertion requires a chat model.");

		RefusalEvaluator evaluator = new RefusalEvaluator(evaluatedModel, judgeModel);
		double totalScore = 0.0;
		int unfairRuns = 0;
		int firstRefusals = 0;
		int secondRefusals = 0;

		for (int run = 0; run < runs; run++)
		{
			Sample sample = buildRefusalSample(evaluatedModel != null);
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
		ChatModel judgeModel = requireChatModel("False Positive Rate Disparity assertion requires a chat model.");
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

	public AssertionEvaluation assertLocalGenderBias(double maxAbsoluteValue)
	{
		LocalGenderBiasEvaluator evaluator = new LocalGenderBiasEvaluator();
		Sample sample = buildSingleSample();
		Evaluation evaluation = evaluator.evaluate(sample);

		if (Math.abs(evaluation.getValue()) > maxAbsoluteValue)
		{
			throw new Rage4JLocalGenderBiasException(
				ABSOLUTE_MAXVALUE + evaluation.getValue() + ", Allowed: " + maxAbsoluteValue);
		}
		return AssertionEvaluation.from(evaluation, this);
	}

	/**
	 * Asserts that the implicit/explicit bias score for the given category does not exceed
	 * {@code maxBiasScore}.
	 *
	 * <p>In {@code "EXPLICIT"} mode two finished prompt variants are generated from the same
	 * base scenario and differ only in the protected attribute. The evaluated model answers both
	 * prompts, and the evaluator compares the parsed 1-to-10 scores.
	 *
	 * <p>In {@code "IMPLICIT"} mode the evaluated model answers two finished prompt variants by
	 * selecting adjectives from a mixed word bank. The evaluator compares the adjective scores of
	 * both generated answers.
	 *
	 * @param category        one of the predefined bias categories ({@code "GENDER"}, {@code "AGE"}, …)
	 * @param mode            {@code "EXPLICIT"} or {@code "IMPLICIT"}
	 * @param maxBiasScore    maximum allowed absolute bias score (0–1); the assertion fails when the
	 *                        measured score exceeds this threshold
	 */
	public AssertionEvaluation assertImplicitExplicitBias(String category, String mode,
		double maxBiasScore)
	{
		return assertImplicitExplicitBias(category, mode, maxBiasScore, DEFAULT_RUNS_PER_SCENARIO);
	}

	public AssertionEvaluation assertImplicitExplicitBias(GroupPair groupPair, String mode,
		double maxBiasScore)
	{
		return assertImplicitExplicitBias(groupPair, mode, maxBiasScore, DEFAULT_RUNS_PER_SCENARIO);
	}

	public AssertionEvaluation assertImplicitExplicitBias(String category, String mode,
		double maxBiasScore, GroupPair groupPair)
	{
		return assertImplicitExplicitBias(category, mode, maxBiasScore, DEFAULT_RUNS_PER_SCENARIO, groupPair);
	}

	public AssertionEvaluation assertImplicitExplicitBias(GroupPair groupPair, String mode,
		double maxBiasScore, int runs)
	{
		return assertImplicitExplicitBias("CUSTOM", mode, maxBiasScore, runs, groupPair);
	}

	public AssertionEvaluation assertImplicitExplicitBias(GroupPair groupPair, String mode,
		double maxBiasScore, int runs, List<String> positiveAdjectives, List<String> negativeAdjectives,
		List<String> neutralAdjectives)
	{
		return assertImplicitExplicitBias("CUSTOM", mode, maxBiasScore, runs, groupPair,
			positiveAdjectives, negativeAdjectives, neutralAdjectives);
	}

	public AssertionEvaluation assertImplicitExplicitBias(ConfiguredGroupPair configuredGroupPair, String mode,
		double maxBiasScore)
	{
		return assertImplicitExplicitBias(configuredGroupPair, mode, maxBiasScore, DEFAULT_RUNS_PER_SCENARIO);
	}

	public AssertionEvaluation assertImplicitExplicitBias(ConfiguredGroupPair configuredGroupPair, String mode,
		double maxBiasScore, int runs)
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

	public AssertionEvaluation assertImplicitExplicitBias(String category, String mode,
		double maxBiasScore, int runs, List<String> positiveAdjectives, List<String> negativeAdjectives,
		List<String> neutralAdjectives)
	{
		return assertImplicitExplicitBias(category, mode, maxBiasScore, runs, null,
			positiveAdjectives, negativeAdjectives, neutralAdjectives);
	}

	public AssertionEvaluation assertImplicitExplicitBias(String category, String mode,
		double maxBiasScore, int runs)
	{
		return assertImplicitExplicitBias(category, mode, maxBiasScore, runs, null);
	}

	public AssertionEvaluation assertImplicitExplicitBias(String category, String mode,
		double maxBiasScore, int runs, GroupPair groupPair)
	{
		return assertImplicitExplicitBias(category, mode, maxBiasScore, runs, groupPair, null, null, null);
	}

	public AssertionEvaluation assertImplicitExplicitBias(String category, String mode,
		double maxBiasScore, int runs, GroupPair groupPair, List<String> positiveAdjectives,
		List<String> negativeAdjectives, List<String> neutralAdjectives)
	{
		if (runs <= 0)
		{
			throw new IllegalArgumentException("runs must be greater than 0");
		}

		ChatModel evaluatedModel = requireChatModel("Implicit/explicit bias assertion requires an evaluated chat model.");
		ChatModel normalizationModel =
			requireJudgeChatModel("Implicit/explicit bias assertion requires a second chat model for normalization.");
		EvaluatedPromptPair evaluatedPromptPair = selectUsablePromptPair(
			category, mode, evaluatedModel, normalizationModel, groupPair, positiveAdjectives, negativeAdjectives, neutralAdjectives);
		if (evaluatedPromptPair == null)
		{
			throw new IllegalStateException("Implicit/explicit bias assertion produced no valid runs.");
		}

		ImplicitExplicitBiasEvaluator evaluator =
			new ImplicitExplicitBiasEvaluator(
				category,
				mode,
				evaluatedPromptPair.promptPair().groupPair(),
				normalizationModel,
				positiveAdjectives,
				negativeAdjectives,
				neutralAdjectives);
		double totalBiasScore = 0.0;
		int validRuns = 0;
		Map<String, Integer> firstWordCounts = new HashMap<>();
		Map<String, Integer> secondWordCounts = new HashMap<>();
		Set<String> allowedAdjectives = ImplicitExplicitBiasEvaluator.IMPLICIT.equals(mode)
			? buildAllowedAdjectiveSet(category, positiveAdjectives, negativeAdjectives, neutralAdjectives)
			: Set.of();

		for (int run = 0; run < runs; run++)
		{
			EvaluatedPromptPair currentRun;
			if (run == 0)
			{
				currentRun = evaluatedPromptPair;
			}
			else
			{
				currentRun = evaluatePromptPair(evaluatedPromptPair.promptPair(), evaluatedModel, evaluator);
			}

			if (currentRun.evaluation() != null)
			{
				Evaluation runEvaluation = currentRun.evaluation();
				totalBiasScore += runEvaluation.getValue();
				validRuns++;
				if (ImplicitExplicitBiasEvaluator.IMPLICIT.equals(mode))
				{
					collectAdjectiveFrequencies(currentRun.normalizedFirstAnswer(), allowedAdjectives, firstWordCounts);
					collectAdjectiveFrequencies(currentRun.normalizedSecondAnswer(), allowedAdjectives, secondWordCounts);
				}
			}
			else
			{
				LOG.info("Implicit/explicit bias run {} was skipped: {}", run + 1, currentRun.failureReason());
				logInvalidImplicitExplicitRun(currentRun);
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
			evaluatedPromptPair.promptPair().groupPair().firstGroup(),
			evaluatedPromptPair.promptPair().groupPair().secondGroup());

		LOG.info("Implicit/explicit bias aggregate [{}]: mode={}, firstGroup='{}', secondGroup='{}', biasScore={}, validRuns={}, configuredRuns={}, preferredGroup='{}'",
			category, mode, evaluatedPromptPair.promptPair().groupPair().firstGroup(),
			evaluatedPromptPair.promptPair().groupPair().secondGroup(), fmt(biasScore), validRuns, runs, preferredGroup);
		if (ImplicitExplicitBiasEvaluator.IMPLICIT.equals(mode))
		{
			LOG.info("Implicit/explicit bias top adjectives [{}]: firstTopWords='{}', secondTopWords='{}'",
				category, formatTopWords(firstWordCounts), formatTopWords(secondWordCounts));
		}

		if (Math.abs(biasScore) > maxBiasScore)
		{
			throw new Rage4JImplicitExplicitBiasException(
				ABSOLUTE_MAXVALUE + biasScore + ", Allowed: " + maxBiasScore + ", Preferred group: " + preferredGroup);
		}
		return AssertionEvaluation.from(evaluation, this);
	}

	private EvaluatedPromptPair selectUsablePromptPair(String category, String mode, ChatModel evaluatedModel,
		ChatModel normalizationModel,
		GroupPair groupPair, List<String> positiveAdjectives, List<String> negativeAdjectives, List<String> neutralAdjectives)
	{
		String baseScenario = optionalQuestion();
		if (groupPair != null)
		{
			PromptPair promptPair = buildPromptPair(category, mode, baseScenario, groupPair, positiveAdjectives, negativeAdjectives, neutralAdjectives);
			ImplicitExplicitBiasEvaluator evaluator = new ImplicitExplicitBiasEvaluator(
				category,
				mode,
				promptPair.groupPair(),
				normalizationModel,
				positiveAdjectives,
				negativeAdjectives,
				neutralAdjectives);
			EvaluatedPromptPair evaluatedPromptPair = evaluatePromptPair(promptPair, evaluatedModel, evaluator);
			if (evaluatedPromptPair.evaluation() != null)
			{
				return evaluatedPromptPair;
			}
			LOG.info("Implicit/explicit bias initial prompt pair was unusable.");
			logInvalidImplicitExplicitRun(evaluatedPromptPair);
			return null;
		}

		for (PromptPair promptPair : ImplicitExplicitBiasPromptBuilder.buildPresetPromptPairs(category, mode, baseScenario))
		{
			if (ImplicitExplicitBiasEvaluator.IMPLICIT.equals(mode)
				&& positiveAdjectives != null && negativeAdjectives != null && neutralAdjectives != null)
			{
				promptPair = buildPromptPair(category, mode, baseScenario, promptPair.groupPair(),
					positiveAdjectives, negativeAdjectives, neutralAdjectives);
			}
			ImplicitExplicitBiasEvaluator evaluator = new ImplicitExplicitBiasEvaluator(
				category,
				mode,
				promptPair.groupPair(),
				normalizationModel,
				positiveAdjectives,
				negativeAdjectives,
				neutralAdjectives);
			EvaluatedPromptPair evaluatedPromptPair = evaluatePromptPair(promptPair, evaluatedModel, evaluator);
			if (evaluatedPromptPair.evaluation() != null)
			{
				return evaluatedPromptPair;
			}
			LOG.info("Implicit/explicit bias preset prompt pair was unusable, trying next fallback.");
			logInvalidImplicitExplicitRun(evaluatedPromptPair);
		}
		return null;
	}

	private PromptPair buildPromptPair(String category, String mode, String baseScenario, GroupPair groupPair,
		List<String> positiveAdjectives, List<String> negativeAdjectives, List<String> neutralAdjectives)
	{
		if (ImplicitExplicitBiasEvaluator.IMPLICIT.equals(mode)
			&& positiveAdjectives != null && negativeAdjectives != null && neutralAdjectives != null)
		{
			List<String> adjectiveList = new java.util.ArrayList<>();
			adjectiveList.addAll(positiveAdjectives);
			adjectiveList.addAll(negativeAdjectives);
			adjectiveList.addAll(neutralAdjectives);
			return ImplicitExplicitBiasPromptBuilder.buildPromptPair(mode, baseScenario, groupPair, adjectiveList);
		}
		if (ImplicitExplicitBiasEvaluator.IMPLICIT.equals(mode))
		{
			return ImplicitExplicitBiasPromptBuilder.buildPromptPair(category, mode, baseScenario, groupPair);
		}
		return ImplicitExplicitBiasPromptBuilder.buildPromptPair(mode, baseScenario, groupPair);
	}

	private EvaluatedPromptPair evaluatePromptPair(PromptPair promptPair, ChatModel evaluatedModel,
		ImplicitExplicitBiasEvaluator evaluator)
	{
		String rawFirstAnswer = evaluatedModel.chat(promptPair.firstPrompt());
		String rawSecondAnswer = evaluatedModel.chat(promptPair.secondPrompt());
		Sample sample = Sample.builder()
			.withQuestion(promptPair.firstPrompt())
			.withAnswer(rawFirstAnswer)
			.withComparisonSample(Sample.builder()
				.withQuestion(promptPair.secondPrompt())
				.withAnswer(rawSecondAnswer)
				.build())
			.build();
		try
		{
			Evaluation evaluation = evaluator.evaluate(sample);
			EvaluatedPromptPair evaluatedPromptPair = new EvaluatedPromptPair(
				promptPair,
				rawFirstAnswer,
				rawSecondAnswer,
				evaluator.getLastFirstNormalizedAnswer(),
				evaluator.getLastSecondNormalizedAnswer(),
				evaluation,
				null);
			logSuccessfulImplicitExplicitRun(evaluatedPromptPair);
			return evaluatedPromptPair;
		}
		catch (IllegalStateException e)
		{
			return new EvaluatedPromptPair(
				promptPair,
				rawFirstAnswer,
				rawSecondAnswer,
				evaluator.getLastFirstNormalizedAnswer(),
				evaluator.getLastSecondNormalizedAnswer(),
				null,
				e.getMessage());
		}
	}

	private void logSuccessfulImplicitExplicitRun(EvaluatedPromptPair evaluatedPromptPair)
	{
		LOG.info("Run bias score: {}", fmt(evaluatedPromptPair.evaluation().getValue()));
	}

	private void logInvalidImplicitExplicitRun(EvaluatedPromptPair evaluatedPromptPair)
	{
		if (evaluatedPromptPair.failureReason() != null)
		{
			LOG.info("Failure reason: {}", evaluatedPromptPair.failureReason());
		}
	}

	private Set<String> buildAllowedAdjectiveSet(String category, List<String> positiveAdjectives, List<String> negativeAdjectives,
		List<String> neutralAdjectives)
	{
		AdjectivePreset adjectivePreset = ImplicitExplicitBiasTemplateLibrary.adjectivePresetFor(category);
		List<String> positive = positiveAdjectives != null ? positiveAdjectives : adjectivePreset.positiveAdjectives();
		List<String> negative = negativeAdjectives != null ? negativeAdjectives : adjectivePreset.negativeAdjectives();
		List<String> neutral = neutralAdjectives != null ? neutralAdjectives : adjectivePreset.neutralAdjectives();
		java.util.ArrayList<String> all = new java.util.ArrayList<>();
		all.addAll(positive);
		all.addAll(negative);
		all.addAll(neutral);
		return all.stream()
			.map(this::normalizeAdjectiveEntry)
			.collect(Collectors.toSet());
	}

	private void collectAdjectiveFrequencies(String response, Set<String> allowedAdjectives, Map<String, Integer> counts)
	{
		if (response == null || response.isBlank())
		{
			return;
		}

		String[] entries = response.split(",");
		for (String entry : entries)
		{
			String cleaned = normalizeAdjectiveEntry(entry);
			if (cleaned.isEmpty() || !allowedAdjectives.contains(cleaned))
			{
				continue;
			}
			counts.merge(cleaned, 1, Integer::sum);
		}
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
		return entry.toLowerCase()
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

	private ChatModel requireChatModel(String message)
	{
		if (chatModel == null)
		{
			throw new IllegalStateException(message);
		}
		return chatModel;
	}

	private ChatModel requireJudgeChatModel(String message)
	{
		if (judgeChatModel == null)
		{
			throw new IllegalStateException(message);
		}
		return judgeChatModel;
	}

	private String optionalQuestion()
	{
		if (question == null || question.trim().isEmpty())
		{
			return "";
		}
		return question;
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

	private Sample buildRefusalSample(boolean regenerateAnswers)
	{
		if (!regenerateAnswers)
		{
			return buildPairedSample();
		}

		if (comparisonQuestion == null || comparisonQuestion.trim().isEmpty())
		{
			throw new IllegalStateException("Comparison question is required for paired bias assertions.");
		}
		if (question == null || question.trim().isEmpty())
		{
			throw new IllegalStateException("Question is required for paired bias assertions.");
		}

		Sample comparisonSample = Sample.builder()
			.withQuestion(comparisonQuestion)
			.build();
		return Sample.builder()
			.withQuestion(question)
			.withComparisonSample(comparisonSample)
			.build();
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

	private record EvaluatedPromptPair(
		PromptPair promptPair,
		String rawFirstAnswer,
		String rawSecondAnswer,
		String normalizedFirstAnswer,
		String normalizedSecondAnswer,
		Evaluation evaluation,
		String failureReason)
	{
	}
}
