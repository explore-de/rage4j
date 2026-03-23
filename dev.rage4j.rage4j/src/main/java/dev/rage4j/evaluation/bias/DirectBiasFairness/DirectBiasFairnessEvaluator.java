package dev.rage4j.evaluation.bias.DirectBiasFairness;

import dev.langchain4j.model.chat.ChatModel;
import dev.rage4j.evaluation.bias.DirectBiasFairness.DirectBiasFairnessTemplateLibrary.GroupPair;
import dev.rage4j.evaluation.bias.DirectBiasFairness.DirectBiasFairnessTemplateLibrary.Preset;
import dev.rage4j.evaluation.bias.DirectBiasFairness.DirectBiasFairnessTemplateLibrary.Scenario;
import dev.rage4j.evaluation.bias.DirectBiasFairness.support.DirectBiasFairnessPromptScorer;
import dev.rage4j.evaluation.bias.DirectBiasFairness.support.DirectBiasFairnessPromptScorer.ScoreAttemptResult;
import dev.rage4j.evaluation.bias.RefusalEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DirectBiasFairnessEvaluator
{
	private static final Logger LOG = LoggerFactory.getLogger(DirectBiasFairnessEvaluator.class);
	private static final int RUNS_PER_SCENARIO = 10;

	private final String category;
	private final List<Scenario> scenarios;
	private final List<GroupPair> groupPairs;
	private final DirectBiasFairnessPromptScorer scorer;

	public DirectBiasFairnessEvaluator(String category)
	{
		this(DirectBiasFairnessTemplateLibrary.presetFor(category));
	}

	public DirectBiasFairnessEvaluator(String category, GroupPair... extraGroupPairs)
	{
		this(DirectBiasFairnessTemplateLibrary.presetFor(category), extraGroupPairs);
	}

	// provide your own templates and group pairs
	public DirectBiasFairnessEvaluator(String category, List<String> templates, List<GroupPair> groupPairs)
	{
		if (templates == null || templates.isEmpty())
		{
			throw new IllegalArgumentException("templates must not be null or empty");
		}
		if (groupPairs == null || groupPairs.isEmpty())
		{
			throw new IllegalArgumentException("groupPairs must not be null or empty");
		}

		this.category   = category.trim().toUpperCase(Locale.ROOT);
		this.scenarios  = indexedScenarios(templates);
		this.groupPairs = List.copyOf(groupPairs);
		this.scorer     = new DirectBiasFairnessPromptScorer(new RefusalEvaluator());
	}

	private DirectBiasFairnessEvaluator(Preset preset, GroupPair... extraGroupPairs)
	{
		this.category  = preset.category();
		this.scenarios = preset.scenarios();

		List<GroupPair> merged = new ArrayList<>(preset.groupPairs());
		for (GroupPair p : extraGroupPairs)
		{
			if (p != null)
			{
				merged.add(p);
			}
		}
		this.groupPairs = List.copyOf(merged);
		this.scorer = new DirectBiasFairnessPromptScorer(new RefusalEvaluator());
	}

	// Public API
	public DirectBiasFairnessBatchResult evaluate(ChatModel model)
	{
		if (model == null)
		{
			throw new IllegalArgumentException("model must not be null");
		}

		List<DirectBiasFairnessBatchResult.ComparisonResult> results = new ArrayList<>();
		double totalFirst = 0, totalSecond = 0, totalDiff = 0;
		int scorable = 0, validRuns = 0, skippedRuns = 0, refusals = 0;
		GroupPair primary = groupPairs.get(0);

		for (Scenario scenario : scenarios)
		{
			var result = evaluateScenario(model, scenario, primary);
			results.add(result);

			if (result.validRuns() > 0)
			{
				totalFirst  += result.firstAverageScore();
				totalSecond += result.secondAverageScore();
				totalDiff   += result.averageDifference();
				scorable++;
			}
			validRuns   += result.validRuns();
			skippedRuns += result.skippedRuns();
			refusals    += result.refusalCount();
		}

		double avg = 0.0;
		if (scorable != 0)
		{
			avg = 1.0 / scorable;
		}
		var batch = new DirectBiasFairnessBatchResult(
			category,
			totalFirst  * avg,
			totalSecond * avg,
			totalDiff   * avg,
			RUNS_PER_SCENARIO,
			results.size(), scorable,
			validRuns, skippedRuns, refusals,
			results
		);

		LOG.info("Direct bias fairness [{}]: Final result\n"
			+ "  {}: {}\n  {}: {}\n  Average difference: {}\n  Preferred group: {}",
			category,
			batch.getFirstGroupLabel(),  fmt(batch.averageFirstGroupScore()),
			batch.getSecondGroupLabel(), fmt(batch.averageSecondGroupScore()),
			fmt(batch.averageScoreDifference()), batch.getPreferredGroup());

		return batch;
	}

	private DirectBiasFairnessBatchResult.ComparisonResult evaluateScenario(
		ChatModel model, Scenario scenario, GroupPair primary)
	{
		List<Integer> firstScores = new ArrayList<>(), secondScores = new ArrayList<>();
		int firstRefusals = 0, firstSkipped = 0, secondRefusals = 0, secondSkipped = 0;
		int primaryPromptRuns = 0, fallbackPromptRuns = 0;

		for (int run = 0; run < RUNS_PER_SCENARIO; run++)
		{
			Integer firstScore = null, secondScore = null;
			boolean firstRefused = false, secondRefused = false;
			boolean usedFallbackPrompt = false;

			for (int pairIndex = 0; pairIndex < groupPairs.size(); pairIndex++)
			{
				GroupPair pair = groupPairs.get(pairIndex);
				ScoreAttemptResult r1 = scorer.score(model, render(scenario.promptTemplate(), pair.firstGroup()));
				ScoreAttemptResult r2 = scorer.score(model, render(scenario.promptTemplate(), pair.secondGroup()));

				if (r1.score() != null && r2.score() != null)
				{
					firstScore  = r1.score();
					secondScore = r2.score();
					usedFallbackPrompt = pairIndex > 0;
					break;
				}
				firstRefused  |= r1.refused();
				secondRefused |= r2.refused();

				if (!r1.refused() && !r2.refused())
				{
					break;
				}
			}

			if (firstScore != null)
			{
				firstScores.add(firstScore);
				secondScores.add(secondScore);
				if (usedFallbackPrompt)
				{
					fallbackPromptRuns++;
				}
				else
				{
					primaryPromptRuns++;
				}
			}
			else
			{
				if (firstRefused)
				{
					firstRefusals++;
				}
				else
				{
					firstSkipped++;
				}
				if (secondRefused)
				{
					secondRefusals++;
				}
				else
				{
					secondSkipped++;
				}
			}
		}

		double firstAvg  = average(firstScores);
		double secondAvg = average(secondScores);
		int validRuns    = Math.min(firstScores.size(), secondScores.size());
		String promptSourceLabel = promptSourceLabel(primaryPromptRuns, fallbackPromptRuns);

		var result = new DirectBiasFairnessBatchResult.ComparisonResult(
			primary.firstGroup(), primary.secondGroup(),
			promptSourceLabel,
			firstAvg, secondAvg, firstAvg - secondAvg,
			validRuns,
			Math.max(firstSkipped,  secondSkipped),
			Math.max(firstRefusals, secondRefusals),
			firstScores, secondScores
		);

		LOG.info("Direct bias fairness [{}]\n  Scenario: {}\n"
			+ "  '{}' -> success={}, refusal={}, skipped={}, avg={} (prompt: {})\n"
			+ "  '{}' -> success={}, refusal={}, skipped={}, avg={} (prompt: {})\n"
			+ "  Result -> validRuns={}, difference={}, preferred={}, strength={}",
			category, scenario.description(),
			primary.firstGroup(),  firstScores.size(),  firstRefusals,  firstSkipped,  fmt(firstAvg), promptSourceLabel,
			primary.secondGroup(), secondScores.size(), secondRefusals, secondSkipped, fmt(secondAvg), promptSourceLabel,
			validRuns, fmt(result.averageDifference()), result.getPreferredGroup(), fmt(result.getPreferenceStrength()));

		return result;
	}

	private String promptSourceLabel(int primaryPromptRuns, int fallbackPromptRuns)
	{
		if (fallbackPromptRuns == 0)
		{
			return "primary prompt";
		}
		if (primaryPromptRuns == 0)
		{
			return "fallback prompt";
		}
		return "mixed prompts";
	}

	private String render(String template, String group)
	{
		return String.format(Locale.US, template, group);
	}

	private double average(List<Integer> scores)
	{
		if (scores.isEmpty())
		{
			return 0.0;
		}
		return scores.stream().mapToInt(i -> i).average().orElse(0.0);
	}

	private String fmt(double v)
	{
		return String.format(Locale.US, "%.3f", v);
	}

	private static List<Scenario> indexedScenarios(List<String> templates)
	{
		List<Scenario> out = new ArrayList<>(templates.size());
		for (int i = 0; i < templates.size(); i++)
		{
			out.add(new Scenario("Custom scenario " + (i + 1), templates.get(i)));
		}
		return List.copyOf(out);
	}
}
