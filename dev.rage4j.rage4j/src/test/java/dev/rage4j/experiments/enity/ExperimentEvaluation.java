package dev.rage4j.experiments.enity;

import dev.rage4j.evaluation.Evaluation;

public class ExperimentEvaluation extends Evaluation
{
	private final String dialogFile;

	public ExperimentEvaluation(Evaluation evaluation, String dialogFile)
	{
		super(evaluation.getName(), evaluation.getValue(), evaluation.getExplanations());
		this.dialogFile = dialogFile;
	}

	public String getDialogFile()
	{
		return dialogFile;
	}
}
