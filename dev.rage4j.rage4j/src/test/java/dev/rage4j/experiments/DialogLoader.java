package dev.rage4j.experiments;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.rage4j.experiments.enity.Dialog;
import dev.rage4j.model.Sample;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class DialogLoader
{
	private int index = 0;
	private final Dialog[] dialogs = loadDialogs();

	public Sample getDialog()
	{
		Dialog dialog = dialogs[index];
		Sample sample = dialog.getSample();
		index = (index + 1) % dialogs.length;
		return sample;
	}

	public Dialog getRawDialog()
	{
		Dialog dialog = dialogs[index];
		index = (index + 1) % dialogs.length;
		return dialog;
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
							Dialog dialog = objectMapper.readValue(path.toFile(), Dialog.class);
							return new Dialog(dialog.dialog(), path.getFileName().toString());
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
}
