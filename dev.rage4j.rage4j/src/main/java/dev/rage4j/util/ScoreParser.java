package dev.rage4j.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ScoreParser
{
	private static final Logger LOG = LoggerFactory.getLogger(ScoreParser.class);

	private ScoreParser()
	{
	}

	public static int parseScore(String score)
	{
		try
		{
			score = score.trim();
			return Integer.parseInt(score);
		}
		catch (NumberFormatException e)
		{
			LOG.warn("Failed to parse score from judge response: '{}'", score, e);
			return 0;
		}

	}
}
