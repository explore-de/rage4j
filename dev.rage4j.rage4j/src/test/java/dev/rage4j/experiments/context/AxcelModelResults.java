package dev.rage4j.experiments.context;

import dev.rage4j.evaluation.axcel.AxcelOneShotExamples;
import dev.rage4j.experiments.enity.ExperimentEvaluation;

import java.util.ArrayList;

public class AxcelModelResults extends ModelResults
{
	AxcelModelResults(String modelName)
	{
		super("axcel-context", modelName);
	}

	protected void addExperimentEvaluation(int context, ExperimentEvaluation experimentEvaluation, AxcelOneShotExamples oneShotExample)
	{
		String mapKey = "context-" + context + "-example-" + oneShotExample.hashCode();
		CONTEXT_EXAMPLE_RESULTS.computeIfAbsent(mapKey, k -> new ArrayList<>()).add(experimentEvaluation);
		super.addExperimentEvaluation(context, experimentEvaluation);
	}
}
