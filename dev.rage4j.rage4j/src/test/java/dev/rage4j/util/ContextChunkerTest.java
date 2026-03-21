package dev.rage4j.util;

import dev.rage4j.LoggingTestWatcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(LoggingTestWatcher.class)
class ContextChunkerTest
{
	@Test
	void testChunkReturnsEmptyListForNullInput()
	{
		List<String> chunks = ContextChunker.chunk(null);

		assertTrue(chunks.isEmpty());
	}

	@Test
	void testChunkReturnsEmptyListForBlankInput()
	{
		List<String> chunks = ContextChunker.chunk("   \n\t  ");

		assertTrue(chunks.isEmpty());
	}

	@Test
	void testChunkFlattensJsonObjectAndArrayPaths()
	{
		String json = """
			{
			  "meta": {
			    "title": "Doc",
			    "author": "Me"
			  },
			  "items": [
			    {"name": "A"},
			    {"name": "B"}
			  ],
			  "empty": "   ",
			  "nullField": null
			}
			""";

		List<String> chunks = ContextChunker.chunk(json);
		String flattened = String.join("\n", chunks);

		assertTrue(chunks.size() >= 1);
		assertTrue(flattened.contains("meta.title = Doc"));
		assertTrue(flattened.contains("meta.author = Me"));
		assertTrue(flattened.contains("items[0].name = A"));
		assertTrue(flattened.contains("items[1].name = B"));
		assertTrue(!flattened.contains("empty ="));
		assertTrue(!flattened.contains("nullField"));
	}

	@Test
	void testChunkFlattensRootArrayJson()
	{
		List<String> chunks = ContextChunker.chunk("[\"alpha\", \"beta\"]");
		String flattened = String.join("\n", chunks);

		assertTrue(flattened.contains("[0] = alpha"));
		assertTrue(flattened.contains("[1] = beta"));
	}

	@Test
	void testChunkFallsBackToPlainTextWhenJsonParsingFails()
	{
		List<String> chunks = ContextChunker.chunk("   {foo:bar}   ");

		assertEquals(1, chunks.size());
		assertEquals("{foo:bar}", chunks.getFirst());
	}

	@Test
	void testChunkUsesParagraphBoundariesWhenParagraphsAreShort()
	{
		String context = "Paragraph one.\n\n\nParagraph two.";

		List<String> chunks = ContextChunker.chunk(context);

		assertEquals(2, chunks.size());
		assertEquals("Paragraph one.", chunks.get(0));
		assertEquals("Paragraph two.", chunks.get(1));
	}

	@Test
	void testChunkCreatesOverlappingChunksForLongParagraph()
	{
		StringBuilder longTextBuilder = new StringBuilder();
		for (int i = 0; i < 1200; i++)
		{
			longTextBuilder.append((char)('a' + (i % 26)));
		}

		List<String> chunks = ContextChunker.chunk(longTextBuilder.toString());

		assertEquals(2, chunks.size());
		assertEquals(1000, chunks.get(0).length());
		assertEquals(350, chunks.get(1).length());
		assertEquals(chunks.get(0).substring(850), chunks.get(1).substring(0, 150));
	}
}

