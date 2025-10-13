package dev.rage4j.experiments.enity;

import dev.rage4j.model.Sample;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public record Dialog(Message[] dialog, String path)
{
	public Sample getSample()
	{
		int length = dialog().length;
		List<String> context = new ArrayList<>();
		// Add all messages except the last two (question and answer) to the context
		for (int i = 0; i < length - 2; i++)
		{
			context.add(messageToString(dialog()[i]));
		}
		return Sample.builder()
			.withQuestion(messageToString(dialog()[length - 2]))
			.withAnswer(messageToString(dialog()[length - 1]))
			.withContext(buildContext(context))
			.build();
	}

	private static @NotNull String buildContext(List<String> contextList)
	{
		StringBuilder sb = new StringBuilder();
		for (String context : contextList)
		{
			sb.append(context).append("\n");
		}
		return sb.toString();
	}

	private static String messageToString(Message message)
	{
		return message.role() + ": " + message.message();
	}
}

