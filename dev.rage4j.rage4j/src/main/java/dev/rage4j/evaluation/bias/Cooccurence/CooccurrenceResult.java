package dev.rage4j.evaluation.bias.Cooccurence;

import java.util.Map;

public class CooccurrenceResult {
    private final double score;

    private final double cobsComponent;

    private final double llmSignal;

    private final Map<String, Double> wordScores;

    public CooccurrenceResult(
            double score,
            double cobsComponent,
            double llmSignal,
            Map<String, Double> wordScores) {
        this.score = score;
        this.cobsComponent = cobsComponent;
        this.llmSignal = llmSignal;
        this.wordScores = wordScores;
    }

    public double getScore() {
        return score;
    }

    public double getCobsComponent() {
        return cobsComponent;
    }

    public double getLlmSignal() {
        return llmSignal;
    }

    public Map<String, Double> getWordScores() {
        return wordScores;
    }

    /**
     * Human-readable direction label.
     */
    public String getDirectionLabel() {
        if (Math.abs(score) < 0.05) return "neutral";
        return score > 0 ? "male-biased" : "female-biased";
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Score : %+.4f  (%s)%n", score, getDirectionLabel()));
        if (!Double.isNaN(cobsComponent)) {
            sb.append(String.format("  COBS component : %+.4f%n", cobsComponent));
        }
        if (llmSignal != 0.0) {
            sb.append(String.format("  LLM signal     : %+.1f  (%s)%n",
                    llmSignal, llmSignal > 0 ? "male" : "female"));
        }
        if (!wordScores.isEmpty()) {
            sb.append("  Top words:%n".formatted());
            wordScores.entrySet().stream()
                    .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                    .limit(5)
                    .forEach(e -> sb.append(String.format("    %-20s %+.4f%n", e.getKey(), e.getValue())));
        }
        return sb.toString();
    }
}
