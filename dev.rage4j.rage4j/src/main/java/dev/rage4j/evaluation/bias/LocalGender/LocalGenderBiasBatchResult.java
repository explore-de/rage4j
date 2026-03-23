package dev.rage4j.evaluation.bias.LocalGender;

import java.util.Collections;
import java.util.List;

public class LocalGenderBiasBatchResult
{
	private final List<String> generatedAnswers;
	private final double averageScore;
	private final double averageRawLogRatio;
	private final double averageFemaleScore;
	private final double averageMaleScore;
	private final int maleBiasedRuns;
	private final int femaleBiasedRuns;
	private final int neutralRuns;
	private final int totalRuns;
	private final List<LocalGenderBiasResult> runResults;

	public LocalGenderBiasBatchResult(
		List<String> generatedAnswers,
		double averageScore,
		double averageRawLogRatio,
		double averageFemaleScore,
		double averageMaleScore,
		int maleBiasedRuns,
		int femaleBiasedRuns,
		int neutralRuns,
		int totalRuns,
		List<LocalGenderBiasResult> runResults)
	{
		this.generatedAnswers = Collections.unmodifiableList(generatedAnswers);
		this.averageScore = averageScore;
		this.averageRawLogRatio = averageRawLogRatio;
		this.averageFemaleScore = averageFemaleScore;
		this.averageMaleScore = averageMaleScore;
		this.maleBiasedRuns = maleBiasedRuns;
		this.femaleBiasedRuns = femaleBiasedRuns;
		this.neutralRuns = neutralRuns;
		this.totalRuns = totalRuns;
		this.runResults = Collections.unmodifiableList(runResults);
	}

	public List<String> getGeneratedAnswers()
	{
		return generatedAnswers;
	}

	public double getAverageScore()
	{
		return averageScore;
	}

	public double getAverageRawLogRatio()
	{
		return averageRawLogRatio;
	}

	public double getAverageFemaleScore()
	{
		return averageFemaleScore;
	}

	public double getAverageMaleScore()
	{
		return averageMaleScore;
	}

	public int getMaleBiasedRuns()
	{
		return maleBiasedRuns;
	}

	public int getFemaleBiasedRuns()
	{
		return femaleBiasedRuns;
	}

	public int getNeutralRuns()
	{
		return neutralRuns;
	}

	public int getTotalRuns()
	{
		return totalRuns;
	}

	public List<LocalGenderBiasResult> getRunResults()
	{
		return runResults;
	}

	public String getOverallDirectionLabel()
	{
		if (averageScore > 0.05)
		{
			return "male-biased";
		}
		if (averageScore < -0.05)
		{
			return "female-biased";
		}
		return "neutral";
	}
}
