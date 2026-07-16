package dev.rage4j.persist.store;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import dev.rage4j.model.EvaluationAggregation;
import dev.rage4j.model.Sample;

class HtmlReportStoreTest
{
	@TempDir
	Path tempDir;

	@Test
	void writesSelfContainedHtmlReport() throws IOException
	{
		Path report = tempDir.resolve("rage4j-report.html");
		HtmlReportStore store = new HtmlReportStore(report);
		EvaluationAggregation aggregation = new EvaluationAggregation(Sample.builder().withQuestion("Is <AI> useful?").withAnswer("Yes & safely").build());
		aggregation.put("Faithfulness", 0.875);

		store.store(aggregation);
		store.close();

		String content = Files.readString(report);
		assertTrue(content.contains("Rage4j evaluation report"));
		assertTrue(content.contains("Is &lt;AI&gt; useful?"));
		assertTrue(content.contains("Yes &amp; safely"));
		assertTrue(content.contains("Faithfulness"));
		assertTrue(content.contains("0.875"));
	}

	@Test
	void preservesEscapedCharactersInLongPreview() throws IOException
	{
		Path report = tempDir.resolve("long-report.html");
		String question = "x".repeat(158) + " & <AI>";
		HtmlReportStore store = new HtmlReportStore(report);
		store.store(new EvaluationAggregation(Sample.builder().withQuestion(question).withAnswer("answer").build()));
		store.close();

		String content = Files.readString(report);
		assertTrue(content.contains("x".repeat(158) + " &amp;…"));
		assertTrue(content.contains(question.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")));
	}
}
