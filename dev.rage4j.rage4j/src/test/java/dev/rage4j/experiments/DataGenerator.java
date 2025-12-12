package dev.rage4j.experiments;

import dev.langchain4j.model.openai.OpenAiChatModel;
import org.junit.jupiter.api.RepeatedTest;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static dev.langchain4j.model.chat.Capability.RESPONSE_FORMAT_JSON_SCHEMA;

public class DataGenerator
{
	private static final String PROMPT = """
		Task 1:
		Write a fictive chat dialog between an AI and a Human. The goal is to demonstrate temporal consistency with the last AI message.
		Consistency goal: %s consistent
		Length: Around %d messages in total.
		Format:
		User:  Message…
		AI: Message…
		
		Task 2:
		Transform this into JSON. Use this format:
		```
		{
		  "dialog": [
		    { "role": "User", "message": "..." },o
		    { "role": "AI", "message": "..." }
		  ]
		}
		```
		Task 3:
		    Take the very last AI message in the dialog. \s
		    Extract each distinct factual claim from that message. \s
		    For each fact:
		        Restate the fact. \s
		        Explain how it relates to earlier information in the dialog. \s
		        Assign a consistency score from 1 to 5, where: \s
		            1 = clearly contradicts earlier content. \s
		            3 = mostly consistent but imperfect or ambiguous. \s
		            5 = fully consistent with earlier content.
		    Present this analysis as a numbered list.
		""";

	private static final String OPEN_AI_KEY = System.getenv("OPEN_AI_KEY");
	private static final OpenAiChatModel chatModel = buildChatModel();

	private static OpenAiChatModel buildChatModel()
	{
		return OpenAiChatModel.builder()
			.apiKey(OPEN_AI_KEY)
			.modelName("gpt-5.1")
			.supportedCapabilities(RESPONSE_FORMAT_JSON_SCHEMA)
			.strictJsonSchema(true)
			.temperature(1.0)
			.build();
	}

	@RepeatedTest(10)
	void generate()
	{
		Path outputDir = Paths.get("target", "generated-dialogs");
		try
		{
			Files.createDirectories(outputDir);
		}
		catch (IOException e)
		{
			throw new UncheckedIOException("Failed to prepare output directory", e);
		}

		for (String level : List.of("very low", "low", "medium", "high"))
		{
			String customizedPrompt = String.format(PROMPT, level, (int)(Math.random() * 20) + 1);
			String response = chatModel.chat(customizedPrompt);
			String md = response.split("```")[1]; // extract JSON part
			md = md.substring(md.indexOf("\n") + 1); // remove first line
			System.out.println("=== Consistency Level: " + level + " ===");
			System.out.println(response);
			System.out.println();

			Path file = outputDir.resolve(level + "-" + System.currentTimeMillis() + ".json");
			try
			{
				Files.writeString(file, md.trim(), StandardCharsets.UTF_8);
			}
			catch (IOException e)
			{
				throw new UncheckedIOException("Failed to write response for level " + level, e);
			}
		}
	}
}
