package dev.rage4j.persist.store;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import dev.rage4j.model.EvaluationAggregation;
import dev.rage4j.persist.EvaluationRecord;
import dev.rage4j.persist.EvaluationStore;
import dev.rage4j.persist.RecordMetadata;

/**
 * An EvaluationStore implementation that writes to a JSON Lines file. Each line
 * is a complete JSON object containing sample data and metrics.
 *
 * <p>
 * JSON Lines (JSONL) format is append-friendly and supports Sample subclasses
 * with nested structures.
 * </p>
 */
public class JsonLinesStore implements EvaluationStore
{

	private static final String NEWLINE = System.lineSeparator();

	private final Path file;
	private final ObjectMapper objectMapper;
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
		store(aggregation, RecordMetadata.now());
	}

	@Override
	public void store(EvaluationAggregation aggregation, RecordMetadata metadata)
	{
		checkNotClosed();
		EvaluationRecord evaluationRecord = EvaluationRecord.from(aggregation, metadata);
		writeRecordWithLock(evaluationRecord);
	}

	private void writeRecordWithLock(EvaluationRecord evaluationRecord)
	{
		try (FileChannel channel = FileChannel.open(file, StandardOpenOption.CREATE, StandardOpenOption.APPEND,
			StandardOpenOption.WRITE); FileLock lock = channel.lock())
		{
			String json = objectMapper.writeValueAsString(evaluationRecord) + NEWLINE;
			Files.writeString(file, json, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
		}
		catch (IOException e)
		{
			throw new UncheckedIOException("Failed to write evaluationRecord to JSONL file", e);
		}
	}

	@Override
	public void flush()
	{
		// No buffering, nothing to flush
	}

	@Override
	public void close()
	{
		closed = true;
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
