package dev.rage4j.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class ContextChunker
{
	private static final ObjectMapper MAPPER = new ObjectMapper();

	private static final int JSON_GROUP_MAX_CHARS = 800;
	private static final int TEXT_MAX_CHARS = 1000;
	private static final int TEXT_OVERLAP_CHARS = 150;
	private static final String JSON_PATH_SEPARATOR = ".";
	private static final String KEY_VALUE_SEPARATOR = " = ";

	private ContextChunker()
	{
	}

	public static List<String> chunk(String context)
	{
		if (isNullOrBlank(context))
		{
			return List.of();
		}

		String trimmed = context.trim();

		if (looksLikeJson(trimmed))
		{
			Optional<List<String>> jsonChunks = tryChunkJson(trimmed);
			if (jsonChunks.isPresent())
			{
				return jsonChunks.get();
			}
		}

		return chunkPlainText(trimmed, TEXT_MAX_CHARS, TEXT_OVERLAP_CHARS);
	}

	private static Optional<List<String>> tryChunkJson(String json)
	{
		try
		{
			JsonNode root = MAPPER.readTree(json);
			List<String> flattened = new ArrayList<>();
			flattenJson(root, "", flattened);
			return Optional.of(groupLines(flattened, JSON_GROUP_MAX_CHARS));
		}
		catch (JsonProcessingException ignored)
		{
			// Fall back to plain text chunking when JSON parsing fails.
			return Optional.empty();
		}
	}

	private static boolean looksLikeJson(String s)
	{
		return (s.startsWith("{") && s.endsWith("}")) || (s.startsWith("[") && s.endsWith("]"));
	}

	private static void flattenJson(JsonNode node, String path, List<String> out)
	{
		if (node == null || node.isNull())
		{
			return;
		}

		if (node.isObject())
		{
			Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
			while (fields.hasNext())
			{
				Map.Entry<String, JsonNode> entry = fields.next();
				String childPath = path.isEmpty() ? entry.getKey() : path + JSON_PATH_SEPARATOR + entry.getKey();
				flattenJson(entry.getValue(), childPath, out);
			}
			return;
		}

		if (node.isArray())
		{
			int index = 0;
			for (JsonNode child : node)
			{
				flattenJson(child, path + "[" + index + "]", out);
				index++;
			}
			return;
		}

		String value = node.asText("");
		if (!value.isBlank())
		{
			out.add(path + KEY_VALUE_SEPARATOR + value);
		}
	}

	private static boolean isNullOrBlank(String value)
	{
		return value == null || value.isBlank();
	}

	private static List<String> groupLines(List<String> lines, int maxChars)
	{
		List<String> chunks = new ArrayList<>();
		StringBuilder current = new StringBuilder();

		for (String line : lines)
		{
			if (line == null || line.isBlank())
			{
				continue;
			}

			appendLine(chunks, current, line, maxChars);
		}

		flushCurrent(chunks, current);
		return chunks;
	}

	private static void appendLine(List<String> chunks, StringBuilder current, String line, int maxChars)
	{
		if (line.length() > maxChars)
		{
			flushCurrent(chunks, current);
			chunks.add(line);
			return;
		}

		int separatorLength = current.isEmpty() ? 0 : 1;
		if (current.length() + separatorLength + line.length() > maxChars)
		{
			flushCurrent(chunks, current);
		}

		if (!current.isEmpty())
		{
			current.append('\n');
		}
		current.append(line);
	}

	private static void flushCurrent(List<String> chunks, StringBuilder current)
	{
		if (!current.isEmpty())
		{
			chunks.add(current.toString());
			current.setLength(0);
		}
	}

	private static List<String> chunkPlainText(String text, int maxChars, int overlapChars)
	{
		String[] paragraphs = text.split("\\n\\n+");
		List<String> nonEmptyParagraphs = new ArrayList<>();

		for (String paragraph : paragraphs)
		{
			String trimmedParagraph = paragraph.trim();
			if (!trimmedParagraph.isBlank())
			{
				nonEmptyParagraphs.add(trimmedParagraph);
			}
		}

		List<String> sourceParts = nonEmptyParagraphs.isEmpty() ? List.of(text) : nonEmptyParagraphs;
		List<String> chunks = new ArrayList<>();

		for (String part : sourceParts)
		{
			appendPart(chunks, part, maxChars, overlapChars);
		}
		return chunks;
	}

	private static void appendPart(List<String> chunks, String part, int maxChars, int overlapChars)
	{
		if (part.length() <= maxChars)
		{
			chunks.add(part);
			return;
		}

		int start = 0;
		while (start < part.length())
		{
			int end = Math.min(start + maxChars, part.length());
			chunks.add(part.substring(start, end));

			boolean isLastChunk = end == part.length();
			start = isLastChunk ? part.length() : Math.max(0, end - overlapChars);
		}
	}
}