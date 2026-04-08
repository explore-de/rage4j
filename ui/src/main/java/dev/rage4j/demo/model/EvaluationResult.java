package dev.rage4j.demo.model;

import java.util.Map;

public class EvaluationResult {
    private String metric;
    private double score;
    private String description;
    private long executionTimeMs;
    private String status;
    private String error;

    public EvaluationResult() {}

    public EvaluationResult(String metric, double score, String description, long executionTimeMs) {
        this.metric = metric;
        this.score = score;
        this.description = description;
        this.executionTimeMs = executionTimeMs;
        this.status = "success";
    }

    public static EvaluationResult error(String metric, String error) {
        EvaluationResult result = new EvaluationResult();
        result.metric = metric;
        result.status = "error";
        result.error = error;
        result.score = 0;
        return result;
    }

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public void setExecutionTimeMs(long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
