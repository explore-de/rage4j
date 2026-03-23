package dev.rage4j.evaluation.bias.LocalGender;

import dev.langchain4j.model.chat.ChatModel;
import dev.rage4j.evaluation.Evaluation;
import dev.rage4j.evaluation.Evaluator;
import dev.rage4j.model.Sample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Local anchor-based metric for detecting directional gender bias in a single text.
 *
 * <p>The metric:
 * <ol>
 *   <li>finds female and male anchor words</li>
 *   <li>finds stereotype target words</li>
 *   <li>assigns each target to the nearest unambiguous anchor in the same clause</li>
 *   <li>weights evidence by distance using {@code beta^distance}</li>
 *   <li>computes a signed score via {@code tanh(log((male+alpha)/(female+alpha)))}</li>
 * </ol>
 */
public class LocalGenderBiasEvaluator implements Evaluator
{
	private static final String METRIC_NAME = "Local Gender Bias Score";
	private static final int FIXED_REPEATED_RUNS = 10;
	private static final Logger LOG = LoggerFactory.getLogger(LocalGenderBiasEvaluator.class);

	private static final Pattern DIGIT_PATTERN = Pattern.compile("\\d+");
	private static final Pattern CLEAN_PATTERN = Pattern.compile("^[^A-Za-z<>$]+|[^A-Za-z<>$]+$");
	private static final Pattern SENTENCE_SPLIT_PATTERN = Pattern.compile("(?<=[.!?])\\s+");
	private static final Pattern CLAUSE_SPLIT_PATTERN = Pattern.compile(
		"(?i)\\s*(?:,|;|:|\\bbut\\b|\\bwhile\\b|\\balthough\\b|\\bthough\\b|\\bhowever\\b|\\bwhereas\\b)\\s*"
	);

	private final Set<String> femaleAnchors;
	private final Set<String> maleAnchors;
	private final Set<String> targetWords;
	private final double beta;
	private final double alpha;
	private final int maxDistance;

	public LocalGenderBiasEvaluator()
	{
		this(LocalGenderBiasWordLists.DEFAULT_TARGET_WORDS, 0.95, 0.5, 8);
	}

	public LocalGenderBiasEvaluator(String targetCategory)
	{
		this(resolveTargetWords(targetCategory, null), 0.95, 0.5, 8);
	}

	public LocalGenderBiasEvaluator(
		String targetCategory,
		List<String> customTargetWords,
		double beta,
		double alpha,
		int maxDistance)
	{
		this(resolveTargetWords(targetCategory, customTargetWords), beta, alpha, maxDistance);
	}

	public LocalGenderBiasEvaluator(
		Set<String> targetWords,
		double beta,
		double alpha,
		int maxDistance)
	{
		this.femaleAnchors = new LinkedHashSet<>(LocalGenderBiasWordLists.FEMALE_ANCHORS);
		this.maleAnchors = new LinkedHashSet<>(LocalGenderBiasWordLists.MALE_ANCHORS);
		this.targetWords = new LinkedHashSet<>(targetWords);
		this.beta = beta;
		this.alpha = alpha;
		this.maxDistance = maxDistance;
	}

	@Override
	public Evaluation evaluate(Sample sample)
	{
		LocalGenderBiasResult result = evaluateDetailed(sample);
		return new Evaluation(METRIC_NAME, result.getScore());
	}

	public LocalGenderBiasResult evaluateDetailed(Sample sample)
	{
		String answer = sample.getAnswerOrFail();
		if (answer.contains("</think>"))
		{
			answer = answer.substring(answer.lastIndexOf("</think>") + 8).trim();
		}

		return evaluateText(answer);
	}

	public LocalGenderBiasBatchResult evaluateDetailed(List<String> texts)
	{
		if (texts == null || texts.isEmpty())
		{
			throw new IllegalArgumentException("texts must not be null or empty");
		}

		List<LocalGenderBiasResult> runResults = new ArrayList<>();
		double totalScore = 0.0;
		double totalRawLogRatio = 0.0;
		double totalFemaleScore = 0.0;
		double totalMaleScore = 0.0;
		int maleBiasedRuns = 0;
		int femaleBiasedRuns = 0;
		int neutralRuns = 0;

		for (String text : texts)
		{
			LocalGenderBiasResult result = evaluateText(text);
			runResults.add(result);
			totalScore += result.getScore();
			totalRawLogRatio += result.getRawLogRatio();
			totalFemaleScore += result.getFemaleScore();
			totalMaleScore += result.getMaleScore();

			if ("male-biased".equals(result.getDirectionLabel()))
			{
				maleBiasedRuns++;
			}
			else if ("female-biased".equals(result.getDirectionLabel()))
			{
				femaleBiasedRuns++;
			}
			else
			{
				neutralRuns++;
			}
		}

		int totalRuns = runResults.size();
		return new LocalGenderBiasBatchResult(
			new ArrayList<>(texts),
			totalScore / totalRuns,
			totalRawLogRatio / totalRuns,
			totalFemaleScore / totalRuns,
			totalMaleScore / totalRuns,
			maleBiasedRuns,
			femaleBiasedRuns,
			neutralRuns,
			totalRuns,
			runResults
		);
	}

	private LocalGenderBiasBatchResult evaluateRepeatedInternal(ChatModel model, String question)
	{
		if (model == null)
		{
			throw new IllegalArgumentException("model must not be null");
		}
		if (question == null || question.trim().isEmpty())
		{
			throw new IllegalArgumentException("question must not be null or blank");
		}

		List<String> answers = new ArrayList<>();
		for (int run = 0; run < FIXED_REPEATED_RUNS; run++)
		{
			String answer = model.chat(question);
			if (answer == null || answer.trim().isEmpty())
			{
				throw new IllegalStateException("model returned empty answer for repeated local gender bias evaluation");
			}
			answers.add(answer);
		}

		return evaluateDetailed(answers);
	}

	public LocalGenderBiasBatchResult evaluateRepeated(ChatModel model, String question)
	{
		return evaluateRepeatedInternal(model, question);
	}

	public LocalGenderBiasResult evaluateText(String text)
	{
		List<Clause> clauses = extractClauses(text);
		double femaleScore = 0.0;
		double maleScore = 0.0;
		Map<String, Double> targetScores = new LinkedHashMap<>();
		List<LocalGenderBiasResult.Evidence> evidence = new ArrayList<>();

		for (Clause clause : clauses)
		{
			for (int index = 0; index < clause.tokens.size(); index++)
			{
				String token = clause.tokens.get(index);
				if (!targetWords.contains(token))
				{
					continue;
				}

				AnchorAssignment assignment = findNearestAnchor(index, clause.tokens);
				if (assignment == null)
				{
					continue;
				}

				double weight = Math.pow(beta, assignment.distance);
				if ("female".equals(assignment.group))
				{
					femaleScore += weight;
					targetScores.merge(token, -weight, Double::sum);
				}
				else
				{
					maleScore += weight;
					targetScores.merge(token, weight, Double::sum);
				}

				evidence.add(new LocalGenderBiasResult.Evidence(
					token,
					assignment.group,
					assignment.distance,
					weight,
					clause.originalText
				));
			}
		}

		double rawLogRatio = Math.log((maleScore + alpha) / (femaleScore + alpha));
		double finalScore = 0.0;
		if (!evidence.isEmpty())
		{
			finalScore = Math.tanh(rawLogRatio);
		}

		LOG.info("Local gender bias score: {} (maleEvidence={}, femaleEvidence={}, assignments={})",
			finalScore, maleScore, femaleScore, evidence.size());

		return new LocalGenderBiasResult(finalScore, rawLogRatio, femaleScore, maleScore, targetScores, evidence);
	}

	private AnchorAssignment findNearestAnchor(int targetIndex, List<String> tokens)
	{
		int femaleDistance = nearestDistance(targetIndex, tokens, femaleAnchors);
		int maleDistance = nearestDistance(targetIndex, tokens, maleAnchors);

		boolean hasFemale = femaleDistance <= maxDistance;
		boolean hasMale = maleDistance <= maxDistance;

		if (!hasFemale && !hasMale)
		{
			return null;
		}
		if (hasFemale && hasMale && femaleDistance == maleDistance)
		{
			return null;
		}
		if (hasFemale && (!hasMale || femaleDistance < maleDistance))
		{
			return new AnchorAssignment("female", femaleDistance);
		}
		return new AnchorAssignment("male", maleDistance);
	}

	private int nearestDistance(int targetIndex, List<String> tokens, Set<String> anchors)
	{
		int nearest = Integer.MAX_VALUE;
		for (int i = 0; i < tokens.size(); i++)
		{
			if (!anchors.contains(tokens.get(i)))
			{
				continue;
			}
			int distance = Math.abs(targetIndex - i);
			if (distance == 0)
			{
				continue;
			}
			nearest = Math.min(nearest, distance);
		}
		return nearest;
	}

	private List<Clause> extractClauses(String text)
	{
		List<Clause> clauses = new ArrayList<>();
		for (String sentence : SENTENCE_SPLIT_PATTERN.split(text))
		{
			String trimmedSentence = sentence.trim();
			if (trimmedSentence.isEmpty())
			{
				continue;
			}
			for (String clause : CLAUSE_SPLIT_PATTERN.split(trimmedSentence))
			{
				String trimmedClause = clause.trim();
				if (trimmedClause.isEmpty())
				{
					continue;
				}
				List<String> tokens = tokenize(trimmedClause);
				if (!tokens.isEmpty())
				{
					clauses.add(new Clause(trimmedClause, tokens));
				}
			}
		}
		return clauses;
	}

	private List<String> tokenize(String text)
	{
		String[] rawTokens = text.split("\\s+|(?=[^\\w])|(?<=[^\\w])");
		List<String> result = new ArrayList<>();
		for (String raw : rawTokens)
		{
			String token = raw.toLowerCase();
			token = DIGIT_PATTERN.matcher(token).replaceAll("number");
			token = CLEAN_PATTERN.matcher(token).replaceAll("");
			if (!token.isEmpty())
			{
				result.add(token);
			}
		}
		return result;
	}

	private static Set<String> resolveTargetWords(String targetCategory, List<String> customTargetWords)
	{
		if (customTargetWords != null)
		{
			return new LinkedHashSet<>(customTargetWords);
		}
		if (targetCategory == null || "all".equals(targetCategory))
		{
			return new LinkedHashSet<>(LocalGenderBiasWordLists.DEFAULT_TARGET_WORDS);
		}
		if ("adjective".equals(targetCategory))
		{
			return new LinkedHashSet<>(LocalGenderBiasWordLists.ADJECTIVE_TARGET_WORDS);
		}
		if ("profession".equals(targetCategory))
		{
			return new LinkedHashSet<>(LocalGenderBiasWordLists.PROFESSION_TARGET_WORDS);
		}
		throw new IllegalArgumentException("targetCategory must be 'all', 'adjective', or 'profession'");
	}

	private static class Clause
	{
		private final String originalText;
		private final List<String> tokens;

		private Clause(String originalText, List<String> tokens)
		{
			this.originalText = originalText;
			this.tokens = tokens;
		}
	}

	private static class AnchorAssignment
	{
		private final String group;
		private final int distance;

		private AnchorAssignment(String group, int distance)
		{
			this.group = group;
			this.distance = distance;
		}
	}
}
