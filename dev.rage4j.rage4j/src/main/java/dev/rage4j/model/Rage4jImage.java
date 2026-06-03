package dev.rage4j.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.langchain4j.data.message.ImageContent;

import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Represents an image attached to a {@link Sample} that should be passed
 * alongside the textual context to vision-capable evaluation LLMs.
 * <p>
 * An instance always has a {@code name} (used for logging and persistence) and
 * either inline data ({@code data} + {@code mimeType}) <em>or</em> a remote
 * {@code url}. The image bytes are never serialised to disk by the persist
 * module – only the metadata is.
 */
public final class Rage4jImage implements Serializable
{
	@Serial
	private static final long serialVersionUID = 1L;

	private final String name;
	private final String mimeType;
	private final URI url;

	@JsonIgnore
	private final transient byte[] data;

	private Rage4jImage(String name, String mimeType, byte[] data, URI url)
	{
		this.name = Objects.requireNonNull(name, "name");
		this.mimeType = mimeType;
		this.data = data;
		this.url = url;
	}

	/**
	 * Loads an image from a local file. Name and MIME type are derived from the
	 * file path.
	 */
	public static Rage4jImage fromPath(Path path)
	{
		Objects.requireNonNull(path, "path");
		String fileName = path.getFileName().toString();
		String mime = guessMimeFromFileName(fileName);
		byte[] bytes;
		try
		{
			bytes = Files.readAllBytes(path);
		}
		catch (IOException e)
		{
			throw new IllegalArgumentException("Could not read image file: " + path, e);
		}
		return new Rage4jImage(fileName, mime, bytes, null);
	}

	/**
	 * Loads an image from a local file with an explicit name override.
	 */
	public static Rage4jImage fromPath(Path path, String name)
	{
		Objects.requireNonNull(path, "path");
		Objects.requireNonNull(name, "name");
		byte[] bytes;
		try
		{
			bytes = Files.readAllBytes(path);
		}
		catch (IOException e)
		{
			throw new IllegalArgumentException("Could not read image file: " + path, e);
		}
		return new Rage4jImage(name, guessMimeFromFileName(name), bytes, null);
	}

	/**
	 * Builds an image from raw bytes plus the explicit MIME type and a name.
	 */
	public static Rage4jImage fromBytes(byte[] data, String mimeType, String name)
	{
		Objects.requireNonNull(data, "data");
		Objects.requireNonNull(mimeType, "mimeType");
		Objects.requireNonNull(name, "name");
		return new Rage4jImage(name, mimeType, data.clone(), null);
	}

	/**
	 * References an image by URL. The name defaults to the last URL segment.
	 */
	public static Rage4jImage fromUrl(String url)
	{
		Objects.requireNonNull(url, "url");
		URI uri = URI.create(url);
		String name = deriveNameFromUri(uri);
		return new Rage4jImage(name, guessMimeFromFileName(name), null, uri);
	}

	/**
	 * References an image by URL with an explicit name override.
	 */
	public static Rage4jImage fromUrl(String url, String name)
	{
		Objects.requireNonNull(url, "url");
		Objects.requireNonNull(name, "name");
		URI uri = URI.create(url);
		return new Rage4jImage(name, guessMimeFromFileName(name), null, uri);
	}

	public String getName()
	{
		return name;
	}

	public String getMimeType()
	{
		return mimeType;
	}

	public URI getUrl()
	{
		return url;
	}

	@JsonIgnore
	public byte[] getData()
	{
		return data == null ? null : data.clone();
	}

	@JsonIgnore
	public boolean hasInlineData()
	{
		return data != null;
	}

	/**
	 * Maps this image to a LangChain4j {@link ImageContent} suitable for
	 * sending in a multimodal {@code UserMessage}.
	 */
	public ImageContent toImageContent()
	{
		if (url != null)
		{
			return ImageContent.from(url);
		}
		String base64 = java.util.Base64.getEncoder().encodeToString(data);
		return ImageContent.from(base64, mimeType);
	}

	private static String deriveNameFromUri(URI uri)
	{
		String path = uri.getPath();
		if (path == null || path.isBlank() || path.equals("/"))
		{
			return uri.toString();
		}
		int slash = path.lastIndexOf('/');
		String segment = slash >= 0 ? path.substring(slash + 1) : path;
		return segment.isBlank() ? uri.toString() : segment;
	}

	private static String guessMimeFromFileName(String name)
	{
		String lower = name.toLowerCase();
		if (lower.endsWith(".png"))
		{
			return "image/png";
		}
		if (lower.endsWith(".jpg") || lower.endsWith(".jpeg"))
		{
			return "image/jpeg";
		}
		if (lower.endsWith(".gif"))
		{
			return "image/gif";
		}
		if (lower.endsWith(".webp"))
		{
			return "image/webp";
		}
		if (lower.endsWith(".bmp"))
		{
			return "image/bmp";
		}
		return "application/octet-stream";
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (o == null || getClass() != o.getClass())
		{
			return false;
		}
		Rage4jImage that = (Rage4jImage)o;
		return Objects.equals(name, that.name)
			&& Objects.equals(mimeType, that.mimeType)
			&& Objects.equals(url, that.url)
			&& java.util.Arrays.equals(data, that.data);
	}

	@Override
	public int hashCode()
	{
		int result = Objects.hash(name, mimeType, url);
		result = 31 * result + java.util.Arrays.hashCode(data);
		return result;
	}

	@Override
	public String toString()
	{
		return "Rage4jImage{name='" + name + "', mimeType='" + mimeType + "', url=" + url + ", inline=" + hasInlineData() + "}";
	}
}