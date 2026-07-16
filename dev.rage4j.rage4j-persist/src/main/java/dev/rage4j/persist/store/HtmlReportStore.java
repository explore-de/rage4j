package dev.rage4j.persist.store;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import dev.rage4j.model.EvaluationAggregation;
import dev.rage4j.persist.EvaluationStore;

/**
 * An {@link EvaluationStore} that writes a self-contained HTML evaluation
 * report. The generated file has no external dependencies, so it can be
 * archived and opened directly from a Jenkins build.
 */
public class HtmlReportStore implements EvaluationStore
{
	private final Path file;
	private final List<EvaluationAggregation> aggregations = new ArrayList<>();
	private boolean closed;

	public HtmlReportStore(Path file)
	{
		this.file = file;
		ensureParentDirectoryExists();
	}

	@Override
	public void store(EvaluationAggregation aggregation)
	{
		checkNotClosed();
		aggregations.add(aggregation);
	}

	@Override
	public void flush()
	{
		checkNotClosed();
		try
		{
			Files.writeString(file, createReport());
		}
		catch (IOException e)
		{
			throw new UncheckedIOException("Failed to write HTML evaluation report", e);
		}
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

	private String createReport()
	{
		StringBuilder report = new StringBuilder("<!doctype html><html lang=\"en\"><head><meta charset=\"utf-8\">");
		report.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">");
		report.append("<title>Rage4j evaluation report</title><style>");
		report.append("body{font-family:system-ui,sans-serif;max-width:1200px;margin:2rem auto;padding:0 1rem;color:#172033}");
		report.append("table{border-collapse:collapse;width:100%;margin:1rem 0}th,td{border:1px solid #d7dce5;padding:.65rem;text-align:left;vertical-align:top}");
		report.append("th{background:#f3f5f8}details{max-width:36rem;white-space:pre-wrap}summary{cursor:pointer} .score{font-variant-numeric:tabular-nums}");
		report.append("</style></head><body><h1>Rage4j evaluation report</h1><p>Generated ");
		report.append(escape(OffsetDateTime.now().toString())).append(" · ").append(aggregations.size()).append(" evaluation(s)</p>");

		if (aggregations.isEmpty())
		{
			report.append("<p>No evaluation results were stored.</p>");
		}
		else
		{
			report.append("<table><thead><tr><th>#</th><th>Question</th><th>Metrics</th><th>Answer</th></tr></thead><tbody>");
			for (int index = 0; index < aggregations.size(); index++)
			{
				EvaluationAggregation aggregation = aggregations.get(index);
				Map<String, Object> sample = aggregation.sampleMap();
				report.append("<tr><td>").append(index + 1).append("</td><td>");
				appendDetail(report, String.valueOf(sample.getOrDefault("question", "—")));
				report.append("</td><td>");
				appendMetrics(report, aggregation.getMetrics());
				report.append("</td><td>");
				appendDetail(report, String.valueOf(sample.getOrDefault("answer", "—")));
				report.append("</td></tr>");
			}
			report.append("</tbody></table>");
		}
		return report.append("</body></html>").toString();
	}

	private void appendMetrics(StringBuilder report, Map<String, Double> metrics)
	{
		if (metrics.isEmpty())
		{
			report.append("—");
			return;
		}
		report.append("<ul>");
		metrics.entrySet().stream().sorted(Map.Entry.comparingByKey(Comparator.naturalOrder())).forEach(entry -> report.append("<li><strong>")
			.append(escape(entry.getKey())).append("</strong>: <span class=\"score\">").append(String.format("%.3f", entry.getValue()))
			.append("</span></li>"));
		report.append("</ul>");
	}

	private void appendDetail(StringBuilder report, String value)
	{
		String escaped = escape(value);
		if (escaped.length() > 160)
		{
			report.append("<details><summary>").append(escaped, 0, 160).append("…</summary>").append(escaped).append("</details>");
		}
		else
		{
			report.append(escaped);
		}
	}

	private String escape(String value)
	{
		return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
	}

	private void ensureParentDirectoryExists()
	{
		Path parent = file.getParent();
		try
		{
			if (parent != null)
			{
				Files.createDirectories(parent);
			}
		}
		catch (IOException e)
		{
			throw new UncheckedIOException("Failed to create directory: " + parent, e);
		}
	}

	private void checkNotClosed()
	{
		if (closed)
		{
			throw new IllegalStateException("Store is closed");
		}
	}
}
