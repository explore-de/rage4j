package dev.rage4j.evaluation.paireval;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.rage4j.model.Sample;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class PairEvalExampleLoader
{
	private static final Logger log = LoggerFactory.getLogger(PairEvalExampleLoader.class);

	public List<Sample> loadExampleData()
	{
		ClassLoader classLoader = getClass().getClassLoader();
		URL resource = classLoader.getResource("paireval/random.3.jsonl");
		List<Sample> samples = new ArrayList<>();

		if (resource == null)
		{
			return samples;
		}

		try
		{
			Path oneShotExampleFolder = Path.of(resource.toURI());
			String exampleList = Files.readString(oneShotExampleFolder);
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

			for (String exampleJson : exampleList.split("\n"))
			{
				PairEvalJsonObject example = objectMapper.readValue(exampleJson, PairEvalJsonObject.class);
				samples.add(Sample.builder()
					.withContext(buildContext(example.history))
					.withAnswer(example.response)
					.build());
			}

			return samples;
		}
		catch (IOException | URISyntaxException e)
		{
			log.error("Failed to load PairEval one-shot examples.", e);
			return samples;
		}
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

	private static class PairEvalJsonObject
	{
		public List<String> history;
		public String response;
	}
}
