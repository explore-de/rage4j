package dev.rage4j.evaluation.axcel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class AxcelDataLoader
{
	private static final Logger log = LoggerFactory.getLogger(AxcelDataLoader.class);

	public AxcelOneShotExamples loadExampleData()
	{
		ClassLoader classLoader = getClass().getClassLoader();
		URL resource = classLoader.getResource("axcel");

		List<Path> exampleFiles = findExamples(resource);
		int randomIndex = (int)(Math.random() * (exampleFiles.size() - 1));
		Path oneShotExampleFolder = exampleFiles.get(randomIndex);
		return loadExample(oneShotExampleFolder);
	}

	public List<AxcelOneShotExamples> loadAllExampleData()
	{
		ClassLoader classLoader = getClass().getClassLoader();
		URL resource = classLoader.getResource("axcel");

		List<Path> exampleFiles = findExamples(resource);
		List<AxcelOneShotExamples> examples = new ArrayList<>();
		for (Path exampleFolder : exampleFiles)
		{
			examples.add(loadExample(exampleFolder));
		}
		return examples;
	}

	private AxcelOneShotExamples loadExample(Path oneShotExampleFolder)
	{
		try
		{
			Path responsePath = oneShotExampleFolder.resolve("response.txt");
			String response = Files.readString(responsePath);

			Path stPath = oneShotExampleFolder.resolve("source-text.txt");
			String st = Files.readString(stPath);

			Path dtPath = oneShotExampleFolder.resolve("derived-text.txt");
			String dt = Files.readString(dtPath);

			return new AxcelOneShotExamples(st, dt, response);
		}
		catch (IOException e)
		{
			log.error("Failed to load Axcel one-shot example data.", e);
			return new AxcelOneShotExamples(null, null, null);
		}
	}

	private List<Path> findExamples(URL resource)
	{
		List<Path> exampleFiles = new ArrayList<>();
		if (resource != null)
		{
			try
			{
				Path path = Paths.get(resource.toURI());
				try (Stream<Path> files = Files.list(path))
				{
					exampleFiles.addAll(files.toList());
				}
			}
			catch (URISyntaxException | IOException ex)
			{
				log.error("Failed to load Axcel example data from classpath 'axcel' folder.", ex);
			}
		}
		return exampleFiles;
	}
}
