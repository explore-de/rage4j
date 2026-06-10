package dev.rage4j.asserts;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.rage4j.asserts.exception.Rage4JBleuScoreException;
import dev.rage4j.asserts.exception.Rage4JCorrectnessException;
import dev.rage4j.asserts.exception.Rage4JFaithfulnessException;
import dev.rage4j.asserts.exception.Rage4JImplicitExplicitException;
import dev.rage4j.asserts.exception.Rage4JRelevanceException;
import dev.rage4j.asserts.exception.Rage4JRefusalException;
import dev.rage4j.asserts.exception.Rage4JRougeScoreException;
import dev.rage4j.asserts.exception.Rage4JSimilarityException;
import dev.rage4j.evaluation.Evaluation;
import dev.rage4j.evaluation.answercorrectness.AnswerCorrectnessEvaluator;
import dev.rage4j.evaluation.answerrelevance.embedding.AnswerRelevanceEmbeddingEvaluator;
import dev.rage4j.evaluation.answersemanticsimilarity.AnswerSemanticSimilarityEvaluator;
import dev.rage4j.evaluation.bleuscore.BleuScoreEvaluator;
import dev.rage4j.evaluation.bias.implicitexplicit.ImplicitExplicitEvaluator;
import dev.rage4j.evaluation.bias.implicitexplicit.support.ImplicitExplicitTemplateLibrary;
import dev.rage4j.evaluation.bias.implicitexplicit.support.ImplicitExplicitTemplateLibrary.AdjectivePreset;
import dev.rage4j.evaluation.bias.implicitexplicit.support.ImplicitExplicitTemplateLibrary.GroupPair;
import dev.rage4j.evaluation.bias.refusal.RefusalEvaluator;
import dev.rage4j.evaluation.faithfulness.FaithfulnessEvaluator;
import dev.rage4j.evaluation.rougescore.RougeScoreEvaluator;
import dev.rage4j.model.EvaluationAggregation;
import dev.rage4j.model.Sample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class RageAssertTestCaseAssertions
{
	private static final Logger LOG = LoggerFactory.getLogger(RageAssertTestCaseAssertions.class);
	private static final String MINVALUE = "Answer did not reach required min value! Evaluated value: ";
	private static final int MAXRETRYCOUNT = 3;

	private final Sample sample;
	private final ImplicitExplicitScenario implicitExplicitScenario;
	private final ChatModel judgeChatModel;
	private final ChatModel evaluatedChatModel;
	private final EmbeddingModel embeddingModel;
	private final boolean evaluationMode;
	private EvaluationAggregation pendingAggregation;

	public RageAssertTestCaseAssertions(Sample sample, ChatModel chatModel, EmbeddingModel embeddingModel, boolean evaluationMode)
	{
		this(sample, null, chatModel, chatModel, embeddingModel, evaluationMode);
	}

	public RageAssertTestCaseAssertions(Sample sample, ImplicitExplicitScenario implicitExplicitScenario, ChatModel judgeChatModel, ChatModel evaluatedChatModel, EmbeddingModel embeddingModel, boolean evaluationMode)
	{
		this.sample = sample;
		this.implicitExplicitScenario = implicitExplicitScenario;
		this.judgeChatModel = judgeChatModel;
		this.evaluatedChatModel = evaluatedChatModel;
		this.embeddingModel = embeddingModel;
		this.evaluationMode = evaluationMode;
	}

	private void handleAssertionFailure(String message, String metricName, Supplier<RuntimeException> exceptionSupplier)
	{
		if (evaluationMode)
		{
			LOG.warn("[EVALUATION MODE] {} assertion failed: {}", metricName, message);
		}
		else
		{
			throw exceptionSupplier.get();
		}
	}

	private void collectEvaluation(Evaluation evaluation)
	{
		if (pendingAggregation == null)
		{
			pendingAggregation = new EvaluationAggregation(sample);
		}
		pendingAggregation.put(evaluation);
	}

	public EvaluationAggregation getEvaluationAggregation()
	{
		return pendingAggregation;
	}

	public AssertionEvaluation assertFaithfulness(double minValue)
	{
		FaithfulnessEvaluator evaluator = new FaithfulnessEvaluator(judgeChatModel, sample.hasImages());
		Evaluation evaluation = evaluator.evaluate(sample);
		boolean passed = minValue <= evaluation.getValue();
		collectEvaluation(evaluation);
		if (!passed)
		{
			String message = MINVALUE + evaluation.getValue() + " answer: " + sample.getAnswer();
			handleAssertionFailure(message, "Faithfulness", () -> new Rage4JFaithfulnessException(message));
		}
		return AssertionEvaluation.from(evaluation, this);
	}

	public AssertionEvaluation assertAnswerCorrectness(double minValue)
	{
		AnswerCorrectnessEvaluator evaluator = new AnswerCorrectnessEvaluator(judgeChatModel);
		Evaluation evaluation = evaluator.evaluate(sample);
		boolean passed = minValue <= evaluation.getValue();
		collectEvaluation(evaluation);
		if (!passed)
		{
			String message = MINVALUE + evaluation.getValue() + " answer: " + sample.getAnswer();
			handleAssertionFailure(message, "Answer Correctness", () -> new Rage4JCorrectnessException(message));
		}
		return AssertionEvaluation.from(evaluation, this);
	}

	public AssertionEvaluation assertAnswerRelevance(double minValue)
	{
		AnswerRelevanceEmbeddingEvaluator evaluator = new AnswerRelevanceEmbeddingEvaluator(judgeChatModel, embeddingModel);
		Evaluation evaluation = evaluator.evaluate(sample);
		boolean passed = minValue <= evaluation.getValue();
		collectEvaluation(evaluation);
		if (!passed)
		{
			String message = assertionFailureMessage(evaluation.getValue(), minValue, sample.getAnswer());
			handleAssertionFailure(message, "Answer Relevance", () -> new Rage4JRelevanceException(message));
		}
		return AssertionEvaluation.from(evaluation, this);
	}

	public AssertionEvaluation assertSemanticSimilarity(double minValue)
	{
		AnswerSemanticSimilarityEvaluator evaluator = new AnswerSemanticSimilarityEvaluator(embeddingModel);
		Evaluation evaluation = evaluator.evaluate(sample);
		boolean passed = minValue <= evaluation.getValue();
		collectEvaluation(evaluation);
		if (!passed)
		{
			String message = assertionFailureMessage(evaluation.getValue(), minValue, sample.getAnswer());
			handleAssertionFailure(message, "Semantic Similarity", () -> new Rage4JSimilarityException(message));
		}
		return AssertionEvaluation.from(evaluation, this);
	}

	public AssertionEvaluation assertBleuScore(double minValue)
	{
		BleuScoreEvaluator evaluator = new BleuScoreEvaluator();
		Evaluation evaluation = evaluator.evaluate(sample);
		boolean passed = minValue <= evaluation.getValue();
		collectEvaluation(evaluation);
		if (!passed)
		{
			String message = assertionFailureMessage(evaluation.getValue(), minValue, sample.getAnswer());
			handleAssertionFailure(message, "BLEU Score", () -> new Rage4JBleuScoreException(message));
		}
		return AssertionEvaluation.from(evaluation, this);
	}

	public AssertionEvaluation assertRougeScore(double minValue, RougeScoreEvaluator.RougeType rougeType, RougeScoreEvaluator.MeasureType measureType)
	{
		RougeScoreEvaluator evaluator = new RougeScoreEvaluator(rougeType, measureType);
		Evaluation evaluation = evaluator.evaluate(sample);
		boolean passed = minValue <= evaluation.getValue();
		collectEvaluation(evaluation);
		if (!passed)
		{
			String message = assertionFailureMessage(evaluation.getValue(), minValue, sample.getAnswer());
			handleAssertionFailure(message, "ROUGE Score", () -> new Rage4JRougeScoreException(message));
		}
		return AssertionEvaluation.from(evaluation, this);
	}

	public AssertionEvaluation assertRefusalDisparity()
	{
		return assertRefusalDisparity(0.0);
	}

	public AssertionEvaluation assertRefusalDisparity(double maxValue)
	{
		return assertRefusalDisparityInternal(1, maxValue);
	}

	public AssertionEvaluation assertRefusalDisparity(int runs)
	{
		return assertRefusalDisparityInternal(runs, 0.0);
	}

	private AssertionEvaluation assertRefusalDisparityInternal(int runs, double maxValue)
	{
		if (runs <= 0)
		{
			throw new IllegalArgumentException("runs must be greater than 0");
		}
		RefusalEvaluator evaluator = new RefusalEvaluator(judgeChatModel);
		double totalScore = 0.0;
		int unfairRuns = 0;
		int successfulRuns = 0;
		int firstRefusals = 0;
		int secondRefusals = 0;
		for (int run = 0; run < runs; run++)
		{
			RefusalEvaluator.DisparityState comparison = evaluateRefusalRun(evaluator, runs);
			if (comparison == null)
			{
				continue;
			}
			successfulRuns++;
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
		if (successfulRuns == 0)
		{
			throw new IllegalStateException("Refusal disparity assertion produced no successful runs.");
		}
		double averageScore = totalScore / successfulRuns;
		Evaluation evaluation = new Evaluation("Refusal Disparity", averageScore);
		collectEvaluation(evaluation);
		LOG.info("");
		LOG.info("Refusal disparity aggregate: firstRefusals={}, secondRefusals={}, configuredRuns={}, successfulRuns={}, unfairRuns={}, averageScore={}", firstRefusals, secondRefusals, runs, successfulRuns, unfairRuns, averageScore);
		assertRefusalDisparityWithin(evaluation, maxValue);
		return AssertionEvaluation.from(evaluation, this);
	}

	public AssertionEvaluation assertImplicitExplicit()
	{
		return assertImplicitExplicit(1);
	}

	public AssertionEvaluation assertImplicitExplicit(int runs)
	{
		if (implicitExplicitScenario == null)
		{
			throw new IllegalStateException("An implicitExplicitScenario must be set before assertImplicitExplicitBias is used.");
		}
		AdjectivePreset adjectivePreset = implicitExplicitScenario.adjectivePreset();
		if (adjectivePreset != null)
		{
			return assertImplicitExplicitInternal(implicitExplicitScenario.category(), implicitExplicitScenario.mode().value(), runs, implicitExplicitScenario.groupPair(), adjectivePreset.positiveAdjectives(), adjectivePreset.negativeAdjectives(), adjectivePreset.neutralAdjectives());
		}
		return assertImplicitExplicitInternal(implicitExplicitScenario.category(), implicitExplicitScenario.mode().value(), runs, implicitExplicitScenario.groupPair(), null, null, null);
	}

	public AssertionEvaluation assertImplicitExplicit(GroupPair groupPair, String mode)
	{
		return assertImplicitExplicitInternal(null, mode, 1, groupPair, null, null, null);
	}

	public AssertionEvaluation assertImplicitExplicit(String category, String mode, GroupPair groupPair)
	{
		return assertImplicitExplicitInternal(category, mode, 1, groupPair, null, null, null);
	}

	public AssertionEvaluation assertImplicitExplicit(GroupPair groupPair, String mode, int runs)
	{
		return assertImplicitExplicitInternal(null, mode, runs, groupPair, null, null, null);
	}

	public AssertionEvaluation assertImplicitExplicit(GroupPair groupPair, String mode, List<String> positiveAdjectives, List<String> negativeAdjectives, List<String> neutralAdjectives)
	{
		return assertImplicitExplicitInternal(null, mode, 1, groupPair, positiveAdjectives, negativeAdjectives, neutralAdjectives);
	}

	public AssertionEvaluation assertImplicitExplicit(GroupPair groupPair, String mode, int runs, List<String> positiveAdjectives, List<String> negativeAdjectives, List<String> neutralAdjectives)
	{
		return assertImplicitExplicitInternal(null, mode, runs, groupPair, positiveAdjectives, negativeAdjectives, neutralAdjectives);
	}

	public AssertionEvaluation assertImplicitExplicit(String category, String mode, int runs, GroupPair groupPair)
	{
		return assertImplicitExplicitInternal(category, mode, runs, groupPair, null, null, null);
	}

	private AssertionEvaluation assertImplicitExplicitInternal(String category, String mode, int runs, GroupPair groupPair, List<String> positiveAdjectives, List<String> negativeAdjectives, List<String> neutralAdjectives)
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
			List<String> positive = positiveAdjectives;
			List<String> negative = negativeAdjectives;
			List<String> neutral = neutralAdjectives;
			if (positive == null || negative == null || neutral == null)
			{
				AdjectivePreset adjectivePreset = ImplicitExplicitTemplateLibrary.adjectivePresetFor(category);
				positive = adjectivePreset.positiveAdjectives();
				negative = adjectivePreset.negativeAdjectives();
				neutral = adjectivePreset.neutralAdjectives();
			}
			ArrayList<String> all = new ArrayList<>();
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
		collectEvaluation(evaluation);
		double biasScore = evaluation.getValue();
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
			LOG.info("top adjectives: topWords='{}'", formatTopWords(wordCounts));
		}
		assertImplicitExplicitWithin(evaluation, 0.0);
		return AssertionEvaluation.from(evaluation, this);
	}

	private void assertRefusalDisparityWithin(Evaluation evaluation, double maxValue)
	{
		if (evaluation.getValue() > maxValue)
		{
			String message = "Answer exceeded allowed max value! Evaluated value: " + evaluation.getValue() + ", Allowed: " + maxValue;
			handleAssertionFailure(message, "Refusal Disparity", () -> new Rage4JRefusalException(message));
		}
	}

	private void assertImplicitExplicitWithin(Evaluation evaluation, double maxAbsoluteValue)
	{
		double absoluteValue = Math.abs(evaluation.getValue());
		if (absoluteValue > maxAbsoluteValue)
		{
			String message = "Answer exceeded allowed max absolute bias! Evaluated value: " + evaluation.getValue() + ", Allowed absolute max: " + maxAbsoluteValue;
			handleAssertionFailure(message, "Implicit/Explicit Bias", () -> new Rage4JImplicitExplicitException(message));
		}
	}

	private String assertionFailureMessage(double value, double minValue, String answer)
	{
		return MINVALUE + value + ", Required: " + minValue + ", Answer: " + answer;
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
		return counts.entrySet().stream()
			.sorted((left, right) -> {
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

	private RefusalEvaluator.DisparityState evaluateRefusalRun(RefusalEvaluator evaluator, int runs)
	{
		int maxAttempts = runs > 1 ? MAXRETRYCOUNT + 1 : 1;
		for (int attempt = 1; attempt <= maxAttempts; attempt++)
		{
			Sample runSample = buildRefusalSampleCheck(runs);
			try
			{
				return evaluator.compare(runSample);
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
			Sample runSample = buildImplicitExplicitSampleCheck(runs);
			try
			{
				return evaluator.evaluate(runSample);
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
		return "answer is refused".equals(message)
			|| "answer is invalid".equals(message)
			|| "Explicit bias evaluation requires both answers to be exact integers from 1 to 10.".equals(message)
			|| "Implicit bias evaluation requires both answers to contain analyzable adjectives.".equals(message);
	}

	private Sample buildRefusalSampleCheck(int runs)
	{
		Sample comparisonSample = sample.getComparisonSampleOrFail();
		if (sample.getAnswer() != null && comparisonSample.getAnswer() != null)
		{
			if (runs <= 1)
			{
				return buildRefusalSample(sample.getAnswer(), comparisonSample.getAnswer());
			}
			if (evaluatedChatModel == null)
			{
				throw new IllegalStateException("Repeated refusal disparity runs require an evaluated chat model so each run can generate fresh answers.");
			}
		}
		if (evaluatedChatModel == null)
		{
			throw new IllegalStateException("Refusal disparity assertion requires either answers or an evaluated chat model.");
		}
		String generatedAnswer = evaluatedChatModel.chat(sample.getQuestion());
		String generatedComparisonAnswer = evaluatedChatModel.chat(comparisonSample.getQuestion());
		return buildRefusalSample(generatedAnswer, generatedComparisonAnswer);
	}

	private Sample buildRefusalSample(String currentAnswer, String currentComparisonAnswer)
	{
		Sample comparisonSample = sample.getComparisonSampleOrFail();
		Sample builtComparisonSample = Sample.builder()
			.withAnswer(currentComparisonAnswer)
			.withQuestion(comparisonSample.getQuestion())
			.withGroundTruth(comparisonSample.getGroundTruth())
			.withContext(comparisonSample.getContext())
			.build();
		return Sample.builder()
			.withAnswer(currentAnswer)
			.withQuestion(sample.getQuestion())
			.withGroundTruth(sample.getGroundTruth())
			.withContext(sample.getContext())
			.withImages(sample.getImages())
			.withComparisonSample(builtComparisonSample)
			.build();
	}

	private Sample buildImplicitExplicitSampleCheck(int runs)
	{
		Sample comparisonSample = sample.getComparisonSampleOrFail();
		if (evaluatedChatModel == null)
		{
			if (runs <= 1 && sample.getAnswer() != null && comparisonSample.getAnswer() != null)
			{
				return buildImplicitExplicitSample(sample.getAnswer(), comparisonSample.getAnswer());
			}
			throw new IllegalStateException("Repeated implicit/explicit bias runs require an evaluated chat model so each run can generate fresh answers.");
		}
		String generatedAnswer = evaluatedChatModel.chat(sample.getQuestion());
		String generatedComparisonAnswer = evaluatedChatModel.chat(comparisonSample.getQuestion());
		return buildImplicitExplicitSample(generatedAnswer, generatedComparisonAnswer);
	}

	private Sample buildImplicitExplicitSample(String currentAnswer, String currentComparisonAnswer)
	{
		Sample comparisonSample = sample.getComparisonSampleOrFail();
		Sample builtComparisonSample = Sample.builder()
			.withAnswer(currentComparisonAnswer)
			.withQuestion(comparisonSample.getQuestion())
			.withGroundTruth(comparisonSample.getGroundTruth())
			.withContext(comparisonSample.getContext())
			.build();
		return Sample.builder()
			.withAnswer(currentAnswer)
			.withQuestion(sample.getQuestion())
			.withGroundTruth(sample.getGroundTruth())
			.withContext(sample.getContext())
			.withImages(sample.getImages())
			.withComparisonSample(builtComparisonSample)
			.build();
	}
}
