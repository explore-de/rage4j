package dev.rage4j.persist.store;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import dev.rage4j.model.EvaluationAggregation;
import dev.rage4j.persist.EvaluationStore;

/**
 * An EvaluationStore implementation that writes to a JSON Lines file. Each line
 * is a complete JSON object containing sample data and metrics.
 *
 * <p>
 * JSON Lines (JSONL) format is append-friendly and supports Sample subclasses
 * with nested structures.
 * </p>
 *
 * <p>
 * This store buffers evaluations in memory and writes them to the file only
 * when {@link #flush()} is called. The {@link #close()} method automatically
 * flushes any remaining buffered data before closing.
 * </p>
 */
public class JsonLinesStore implements EvaluationStore
{

	private static final String NEWLINE = System.lineSeparator();

	private final Path file;
	private final ObjectMapper objectMapper;
	private final List<EvaluationAggregation> buffer;
	private boolean closed;

	/**
	 * Creates a new JsonLinesStore that writes to the specified file.
	 *
	 * @param file
	 *            The path to the JSONL file.
	 */
	public JsonLinesStore(Path file)
	{
		this.file = file;
		this.objectMapper = createObjectMapper();
		this.buffer = new ArrayList<>();
		this.closed = false;
		ensureParentDirectoryExists();
	}

	private ObjectMapper createObjectMapper()
	{
		ObjectMapper mapper = new ObjectMapper();
		mapper.disable(SerializationFeature.INDENT_OUTPUT);
		mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		return mapper;
	}

	private void ensureParentDirectoryExists()
	{
		Path parent = file.getParent();
		if (parent != null && !Files.exists(parent))
		{
			try
			{
				Files.createDirectories(parent);
			}
			catch (IOException e)
			{
				throw new UncheckedIOException("Failed to create directory: " + parent, e);
			}
		}
	}

	@Override
	public void store(EvaluationAggregation aggregation)
	{
		checkNotClosed();
		buffer.add(aggregation);
	}

	/**
	 * Stores an evaluation aggregation and immediately flushes to disk. This is a
	 * convenience method for cases where immediate persistence is needed.
	 *
	 * @param aggregation
	 *            The evaluation aggregation to store.
	 */
	@Override
	public void storeFlush(EvaluationAggregation aggregation)
	{
		store(aggregation);
		flush();
	}

	private void writeBufferWithLock()
	{
		try (FileChannel channel = FileChannel.open(file, StandardOpenOption.CREATE, StandardOpenOption.APPEND,
			StandardOpenOption.WRITE); FileLock lock = channel.lock())
		{
			StringBuilder stringBuilder = new StringBuilder();
			for (EvaluationAggregation aggregation : buffer)
			{
				Map<String, Object> evaluationRecord = new LinkedHashMap<>();
				evaluationRecord.put("sample", aggregation.sampleMap());
				evaluationRecord.put("metrics", aggregation.getMetrics());
				stringBuilder.append(objectMapper.writeValueAsString(evaluationRecord)).append(NEWLINE);
			}
			Files.writeString(file, stringBuilder.toString(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
		}
		catch (IOException e)
		{
			throw new UncheckedIOException("Failed to write evaluations to JSONL file", e);
		}
	}

	@Override
	public void flush()
	{
		checkNotClosed();
		if (buffer.isEmpty())
		{
			return;
		}
		writeBufferWithLock();
		buffer.clear();
	}

	@Override
	public void close()
	{
		if (!closed)
		{
			flush();
			closed = true;
		}
	}

	private void checkNotClosed()
	{
		if (closed)
		{
			throw new IllegalStateException("Store is closed");
		}
	}

	/**
	 * Returns the path to the JSONL file.
	 *
	 * @return The path to the JSONL file.
	 */
	public Path getFile()
	{
		return file;
	}
}
