package dev.rage4j.evaluation.bias.LocalGender;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class LocalGenderBiasResult
{
	private final double score;
	private final double rawLogRatio;
	private final double femaleScore;
	private final double maleScore;
	private final Map<String, Double> targetScores;
	private final List<Evidence> evidence;

	public LocalGenderBiasResult(
		double score,
		double rawLogRatio,
		double femaleScore,
		double maleScore,
		Map<String, Double> targetScores,
		List<Evidence> evidence)
	{
		this.score = score;
		this.rawLogRatio = rawLogRatio;
		this.femaleScore = femaleScore;
		this.maleScore = maleScore;
		this.targetScores = Collections.unmodifiableMap(targetScores);
		this.evidence = Collections.unmodifiableList(evidence);
	}

	public double getScore()
	{
		return score;
	}

	public double getRawLogRatio()
	{
		return rawLogRatio;
	}

	public double getFemaleScore()
	{
		return femaleScore;
	}

	public double getMaleScore()
	{
		return maleScore;
	}

	public Map<String, Double> getTargetScores()
	{
		return targetScores;
	}

	public List<Evidence> getEvidence()
	{
		return evidence;
	}

	public String getDirectionLabel()
	{
		if (evidence.isEmpty() || Math.abs(score) < 0.05)
		{
			return "neutral";
		}
		if (score > 0)
		{
			return "male-biased";
		}
		return "female-biased";
	}

	public static class Evidence
	{
		private final String targetWord;
		private final String anchorGroup;
		private final int distance;
		private final double weight;
		private final String clause;

		public Evidence(String targetWord, String anchorGroup, int distance, double weight, String clause)
		{
			this.targetWord = targetWord;
			this.anchorGroup = anchorGroup;
			this.distance = distance;
			this.weight = weight;
			this.clause = clause;
		}

		public String getTargetWord()
		{
			return targetWord;
		}

		public String getAnchorGroup()
		{
			return anchorGroup;
		}

		public int getDistance()
		{
			return distance;
		}

		public double getWeight()
		{
			return weight;
		}

		public String getClause()
		{
			return clause;
		}
	}
}
