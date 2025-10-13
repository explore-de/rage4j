package dev.rage4j.experiments.context;

import dev.rage4j.experiments.enity.ExperimentEvaluation;

import java.util.ArrayList;

public class PairEvalModelResults extends ModelResults
{

	PairEvalModelResults(String modelName)
	{
		super("paireval-context", modelName);
	}

	protected void addExperimentEvaluation(int context, ExperimentEvaluation experimentEvaluation, int run)
	{
		String mapKey = "context-" + context + "-run-" + run;
		CONTEXT_EXAMPLE_RESULTS.computeIfAbsent(mapKey, k -> new ArrayList<>()).add(experimentEvaluation);
		super.addExperimentEvaluation(context, experimentEvaluation);
	}
}
