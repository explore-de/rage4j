package dev.rage4j.experiments;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.rage4j.model.Sample;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class DialogLoader
{
	private int index = 0;
	private final Dialog[] dialogs = loadDialogs();

	public Sample getDialog()
	{
		Dialog dialog = dialogs[index];
		int length = dialog.dialog.length;
		List<String> context = new ArrayList<>();
		// Add all messages except the last two (question and answer) to the context
		for (int i = 0; i < length - 2; i++)
		{
			context.add(messageToString(dialog.dialog[i]));
		}
		Sample sample = Sample.builder()
			.withQuestion(messageToString(dialog.dialog[length - 2]))
			.withAnswer(messageToString(dialog.dialog[length - 1]))
			.withContextsList(context)
			.build();
		index = index + 1 % dialogs.length;
		return sample;
	}

	private static String messageToString(Message message)
	{
		return message.role + ": " + message.message;
	}

	private Dialog[] loadDialogs()
	{
		try
		{
			Path dialogsPath = Paths.get(getClass().getResource("/generated-dialogs").toURI());
			ObjectMapper objectMapper = new ObjectMapper();

			try (Stream<Path> paths = Files.list(dialogsPath))
			{
				return paths
					.filter(path -> path.toString().endsWith(".json"))
					.map(path -> {
						try
						{
							return objectMapper.readValue(path.toFile(), Dialog.class);
						}
						catch (IOException e)
						{
							throw new RuntimeException("Failed to parse dialog from " + path, e);
						}
					})
					.toArray(Dialog[]::new);
			}
		}
		catch (IOException | URISyntaxException e)
		{
			throw new RuntimeException("Failed to load dialogs", e);
		}
	}

	record Dialog(Message[] dialog)
	{
	}

	record Message(String role, String message)
	{
	}
}
