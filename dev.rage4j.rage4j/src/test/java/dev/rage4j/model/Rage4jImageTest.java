package dev.rage4j.model;

import dev.langchain4j.data.message.ImageContent;
import dev.rage4j.LoggingTestWatcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(LoggingTestWatcher.class)
class Rage4jImageTest
{
	private static final byte[] TEST_BYTES = new byte[] { 1, 2, 3, 4 };

	@Test
	void testFromBytesPopulatesAllFields()
	{
		Rage4jImage image = Rage4jImage.fromBytes(TEST_BYTES, "image/png", "eiffel-tower.png");

		assertEquals("eiffel-tower.png", image.getName());
		assertEquals("image/png", image.getMimeType());
		assertNull(image.getUrl());
		assertTrue(image.hasInlineData());
		assertArrayEquals(TEST_BYTES, image.getData());
	}

	@Test
	void testFromBytesDefensivelyCopiesData()
	{
		byte[] original = new byte[] { 1, 2, 3 };
		Rage4jImage image = Rage4jImage.fromBytes(original, "image/png", "a.png");

		original[0] = 99;
		assertEquals(1, image.getData()[0]);
	}

	@Test
	void testFromUrlDerivesNameFromLastSegment()
	{
		Rage4jImage image = Rage4jImage.fromUrl("https://example.com/path/landmark.jpg");

		assertEquals("landmark.jpg", image.getName());
		assertEquals("image/jpeg", image.getMimeType());
		assertNotNull(image.getUrl());
		assertFalse(image.hasInlineData());
	}

	@Test
	void testFromUrlAllowsExplicitName()
	{
		Rage4jImage image = Rage4jImage.fromUrl("https://example.com/x.png", "renamed.png");
		assertEquals("renamed.png", image.getName());
	}

	@Test
	void testFromPathReadsBytesAndDerivesNameAndMime(@TempDir Path tmp) throws IOException
	{
		Path file = tmp.resolve("notre-dame.JPEG");
		Files.write(file, TEST_BYTES);

		Rage4jImage image = Rage4jImage.fromPath(file);

		assertEquals("notre-dame.JPEG", image.getName());
		assertEquals("image/jpeg", image.getMimeType());
		assertArrayEquals(TEST_BYTES, image.getData());
	}

	@Test
	void testToImageContentForUrlBacked()
	{
		Rage4jImage image = Rage4jImage.fromUrl("https://example.com/x.png");
		ImageContent content = image.toImageContent();

		assertNotNull(content);
		assertEquals("https://example.com/x.png", content.image().url().toString());
	}

	@Test
	void testToImageContentForBytesBacked()
	{
		Rage4jImage image = Rage4jImage.fromBytes(TEST_BYTES, "image/png", "a.png");
		ImageContent content = image.toImageContent();

		assertNotNull(content);
		assertEquals("image/png", content.image().mimeType());
		assertNotNull(content.image().base64Data());
	}

	@Test
	void testGuessMimeFallback()
	{
		Rage4jImage image = Rage4jImage.fromBytes(TEST_BYTES, "image/png", "no-extension");
		// caller-supplied mime takes precedence regardless of name
		assertEquals("image/png", image.getMimeType());

		Rage4jImage byPath = Rage4jImage.fromUrl("https://example.com/file.unknown");
		assertEquals("application/octet-stream", byPath.getMimeType());
	}
}