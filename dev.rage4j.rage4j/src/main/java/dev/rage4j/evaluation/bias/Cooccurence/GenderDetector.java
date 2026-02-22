package dev.rage4j.evaluation.bias.Cooccurence;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import dev.rage4j.evaluation.bias.StereotypicalAssociationsEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Uses an LLM to detect the gender of the main subject in a piece of text,
 * returning a numeric signal that is blended into the COBS score.
 *
 * <ul>
 *   <li>{@code +1.0} — main subject is female</li>
 *   <li>{@code -1.0} — main subject is male</li>
 *   <li>{@code  0.0} — neutral, mixed, or undetectable</li>
 * </ul>
 *
 * <p>Delegates to {@link GenderDetectionBot} via langchain4j {@code AiServices},
 * following the same pattern as {@link StereotypicalAssociationsEvaluator}.
 */
public class GenderDetector
{
	private static final Logger LOG = LoggerFactory.getLogger(GenderDetector.class);

	private final GenderDetectionBot bot;

	public GenderDetector(ChatModel model)
	{
		this.bot = AiServices.create(GenderDetectionBot.class, model);
	}

	public GenderDetector(GenderDetectionBot bot)
	{
		this.bot = bot;
	}

	/**
	 * Detects the gender of the main subject in the given text.
	 *
	 * @param text the text to classify
	 * @return {@code +1.0} (female), {@code -1.0} (male), or {@code 0.0} (neutral/unknown)
	 */
	public double detect(String text)
	{
		try
		{
			String response = bot.detectGender(text).trim().toLowerCase();
			LOG.debug("GenderDetector raw response: '{}'", response);

			if (response.contains("female"))
			{
				return -1.0; // female
			}
			if (response.contains("male"))
			{
				return 1.0; // male
			}
			return 0.0;
		}
		catch (Exception e)
		{
			LOG.warn("LLM call failed, defaulting to neutral: {}", e.getMessage());
			return 0.0;
		}
	}
}
