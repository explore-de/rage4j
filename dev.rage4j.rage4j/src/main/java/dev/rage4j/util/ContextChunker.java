package dev.rage4j.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class ContextChunker
{
	private static final ObjectMapper MAPPER = new ObjectMapper();

	private ContextChunker()
	{
	}

	public static List<String> chunk(String context)
	{
		if (context == null || context.isBlank())
		{
			return List.of();
		}

		String trimmed = context.trim();
		// Try JSON first (robust) and fallback to text if parsing fails
		if (looksLikeJson(trimmed))
		{
			try
			{
				JsonNode root = MAPPER.readTree(trimmed);
				List<String> flattened = new ArrayList<>();
				flattenJson(root, "", flattened);

				// Optional: group tiny lines into bigger chunks (to reduce embed calls)
				return groupLines(flattened, 800);
			}
			catch (JsonProcessingException ignored)
			{
				// fallback below
			}
		}

		// Plain text fallback
		return chunkPlainText(trimmed, 1000, 150);
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
				Map.Entry<String, JsonNode> e = fields.next();
				String childPath = path.isEmpty() ? e.getKey() : path + "." + e.getKey();
				flattenJson(e.getValue(), childPath, out);
			}
			return;
		}

		if (node.isArray())
		{
			int i = 0;
			for (JsonNode child : node)
			{
				String childPath = path + "[" + i + "]";
				flattenJson(child, childPath, out);
				i++;
			}
			return;
		}

		// value node (string/number/boolean)
		String value = node.asText("");
		if (!value.isBlank())
		{
			out.add(path + " = " + value);
		}
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

			// If a single line is huge, just emit it (or you could further split it)
			if (line.length() > maxChars)
			{
				if (!current.isEmpty())
				{
					chunks.add(current.toString());
					current.setLength(0);
				}
				chunks.add(line);
				continue;
			}

			if (current.length() + line.length() + 1 > maxChars)
			{
				if (!current.isEmpty())
				{
					chunks.add(current.toString());
					current.setLength(0);
				}
			}

			if (!current.isEmpty())
			{
				current.append("\n");
			}
			current.append(line);
		}

		if (!current.isEmpty())
		{
			chunks.add(current.toString());
		}

		return chunks;
	}

	private static List<String> chunkPlainText(String text, int maxChars, int overlapChars)
	{
		// 1) split by paragraphs
		String[] paragraphs = text.split("\\n\\n+");
		List<String> initial = new ArrayList<>();
		for (String p : paragraphs)
		{
			String t = p.trim();
			if (!t.isBlank())
			{
				initial.add(t);
			}
		}

		// 2) ensure max size via sliding window chunking
		List<String> chunks = new ArrayList<>();
		for (String part : initial.isEmpty() ? List.of(text) : initial)
		{
			if (part.length() <= maxChars)
			{
				chunks.add(part);
				continue;
			}

			int start = 0;
			while (start < part.length())
			{
				int end = Math.min(start + maxChars, part.length());
				chunks.add(part.substring(start, end));

				if (end == part.length())
				{
					break;
				}
				start = Math.max(0, end - overlapChars);
			}
		}

		return chunks;
	}
}