package dev.rage4j.evaluation.model;

/**
 * Structured response for evaluators that need both a numeric score and a short
 * explanation.
 */
public class ScoreWithReasonResponse
{
	private Integer score;
	private String reason;

	public ScoreWithReasonResponse()
	{
		// no args
	}

	public ScoreWithReasonResponse(Integer score, String reason)
	{
		this.score = score;
		this.reason = reason;
	}

	public Integer getScore()
	{
		return score;
	}

	public void setScore(Integer score)
	{
		this.score = score;
	}

	public String getReason()
	{
		return reason;
	}

	public void setReason(String reason)
	{
		this.reason = reason;
	}
}
