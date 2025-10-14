package dev.rage4j.evaluation.axcel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parses the structured Axcel evaluation response produced by the language
 * model into a list of {@link AxcelFactEvaluation} entries. The parser is
 * tolerant of minor formatting deviations such as missing sections or ratings
 * that are marked as not applicable.
 */
public class AxcelResponseParser
{
	private static final Logger LOG = LoggerFactory.getLogger(AxcelResponseParser.class);
	private static final Pattern HEADER_PATTERN = Pattern.compile("^(\\d+)\\.\\s*(.+)$");
	private static final Pattern RATING_PATTERN = Pattern.compile("Rating:\\s*(\\d+)");
	private static final String BLOCK_SPLIT_REGEX = "\\n{2,}(?=\\d+\\.)";

	/**
	 * Parses the raw response text produced by the Axcel prompt. The response is
	 * expected to contain numbered sections with derived text, source text, and
	 * verification details that include a rating.
	 *
	 * @param rawResponse
	 *            the LLM response to parse.
	 * @return a list of parsed {@link AxcelFactEvaluation} entries. Returns an
	 *         empty list if the input is {@code null} or blank.
	 */
	public List<AxcelFactEvaluation> parse(String rawResponse)
	{
		if (rawResponse == null)
		{
			return List.of();
		}

		String normalized = rawResponse.replace("\r\n", "\n").trim();
		if (normalized.isEmpty())
		{
			return List.of();
		}

		List<String> blocks = splitIntoBlocks(normalized);
		List<AxcelFactEvaluation> results = new ArrayList<>();

		int sequentialIndex = 1;
		for (String block : blocks)
		{
			block = block.trim();
			if (block.isEmpty() || !Character.isDigit(block.charAt(0)))
			{
				continue;
			}
			AxcelFactEvaluation evaluation = parseBlock(block, sequentialIndex);
			results.add(evaluation);
			sequentialIndex++;
		}

		return results;
	}

	private List<String> splitIntoBlocks(String normalized)
	{
		String[] rawBlocks = normalized.split(BLOCK_SPLIT_REGEX);
		if (rawBlocks.length == 0)
		{
			return List.of();
		}

		List<String> blocks = new ArrayList<>(Arrays.asList(rawBlocks));
		// The first block might be a preamble that does not start with "1."
		if (!blocks.isEmpty() && !startsWithDigit(blocks.get(0)))
		{
			blocks.remove(0);
		}
		return blocks;
	}

	private boolean startsWithDigit(String block)
	{
		block = block.stripLeading();
		return !block.isEmpty() && Character.isDigit(block.charAt(0));
	}

	private AxcelFactEvaluation parseBlock(String block, int fallbackIndex)
	{
		String[] lines = block.split("\\n", 2);
		String headerLine = lines.length > 0 ? lines[0].trim() : "";
		Matcher headerMatcher = HEADER_PATTERN.matcher(headerLine);

		int index = fallbackIndex;
		String title = headerLine;

		if (headerMatcher.matches())
		{
			try
			{
				index = Integer.parseInt(headerMatcher.group(1));
			}
			catch (NumberFormatException ex)
			{
				LOG.debug("Unable to parse fact index from header '{}'", headerLine, ex);
			}
			title = headerMatcher.group(2).trim();
		}

		String derivedText = extractSection(block, "Derived Text");
		String sourceText = extractSection(block, "Source Text");
		String verificationSection = extractSection(block, "Verification");

		Integer rating = parseRating(verificationSection);
		String verification = cleanVerificationText(verificationSection);

		return new AxcelFactEvaluation(index, title, derivedText, sourceText, verification, rating);
	}

	private String extractSection(String block, String label)
	{
		String marker = "- **" + label + ":**";
		int start = block.indexOf(marker);
		if (start < 0)
		{
			return "";
		}
		start += marker.length();
		int end = block.indexOf("\n- **", start);
		if (end < 0)
		{
			end = block.length();
		}
		return block.substring(start, end).trim();
	}

	private Integer parseRating(String verificationSection)
	{
		if (verificationSection == null)
		{
			return null;
		}
		Matcher matcher = RATING_PATTERN.matcher(verificationSection);
		if (matcher.find())
		{
			try
			{
				int parsedRating = Integer.parseInt(matcher.group(1));
				return clampRating(parsedRating);
			}
			catch (NumberFormatException ex)
			{
				LOG.debug("Unable to parse rating from '{}'", verificationSection, ex);
			}
		}
		return null;
	}

	private int clampRating(int rating)
	{
		return Math.max(0, Math.min(5, rating));
	}

	private String cleanVerificationText(String verificationSection)
	{
		if (verificationSection == null)
		{
			return "";
		}
		Matcher matcher = RATING_PATTERN.matcher(verificationSection);
		if (matcher.find())
		{
			String withoutRating = verificationSection.substring(0, matcher.start()).trim();
			String trailing = verificationSection.substring(matcher.end()).trim();
			if (!trailing.isEmpty())
			{
				return (withoutRating + " " + trailing).trim();
			}
			return withoutRating;
		}
		return verificationSection.trim();
	}
}
