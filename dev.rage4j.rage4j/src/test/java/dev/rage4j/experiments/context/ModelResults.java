package dev.rage4j.experiments.context;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.rage4j.experiments.StatisticsUtil;
import dev.rage4j.experiments.enity.ExperimentEvaluation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ModelResults
{
	public final String filePrefix;
	public final Map<String, List<ExperimentEvaluation>> CONTEXT_EXAMPLE_RESULTS = new HashMap<>();
	public final Map<String, List<ExperimentEvaluation>> CONTEXT_RESULTS = new HashMap<>();
	public final Map<String, List<String>> ERRORS_BY_CONTEXT = new HashMap<>();
	public final String modelName;

	ModelResults(String filePrefix, String modelName)
	{
		this.filePrefix = filePrefix;
		this.modelName = modelName;
	}

	protected void addExperimentEvaluation(int context, ExperimentEvaluation experimentEvaluation)
	{
		String mapKey = "context-" + context;
		CONTEXT_RESULTS.computeIfAbsent(mapKey, k -> new ArrayList<>()).add(experimentEvaluation);
	}

	public void addError(int context, String dialogPath)
	{
		String mapKey = "context-" + context;
		ERRORS_BY_CONTEXT.computeIfAbsent(mapKey, k -> new ArrayList<>()).add(dialogPath);
	}

	public void storeResults()
	{        // Grouped by context, dialog and one shot example
		List<StatisticsUtil.Stats> statsList = CONTEXT_EXAMPLE_RESULTS.entrySet().stream()
			.map(StatisticsUtil::buildStats)
			.toList();
		StatisticsUtil.writeToFile(statsList, CONTEXT_EXAMPLE_RESULTS, modelName, filePrefix);

		// Grouped by context and dialog only
		statsList = CONTEXT_RESULTS.entrySet().stream()
			.map(StatisticsUtil::buildStats)
			.toList();
		StatisticsUtil.writeToCSV(CONTEXT_RESULTS, modelName + "_reduced", filePrefix);
		StatisticsUtil.writeToFile(statsList, CONTEXT_RESULTS, modelName + "_reduced", filePrefix);

		// Store errors by context
		try
		{
			new ObjectMapper().writeValue(new File("./experiment_results/" + filePrefix + "_" + modelName + "_errors.json"), ERRORS_BY_CONTEXT);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
}
