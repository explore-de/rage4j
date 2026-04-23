package dev.rage4j.demo.service;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.rage4j.demo.model.EvaluationRequest;
import dev.rage4j.demo.model.EvaluationResult;
import dev.rage4j.demo.model.MetricInfo;
import dev.rage4j.evaluation.Evaluation;
import dev.rage4j.evaluation.Evaluator;
import dev.rage4j.evaluation.answercorrectness.AnswerCorrectnessEvaluator;
import dev.rage4j.evaluation.answerrelevance.AnswerRelevanceEvaluator;
import dev.rage4j.evaluation.faithfulness.FaithfulnessEvaluator;
import dev.rage4j.evaluation.answersemanticsimilarity.AnswerSemanticSimilarityEvaluator;
import dev.rage4j.evaluation.bleuscore.BleuScoreEvaluator;
import dev.rage4j.evaluation.rougescore.RougeScoreEvaluator;
import dev.rage4j.model.Sample;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class EvaluationService {

    @ConfigProperty(name = "openai.api.key")
    Optional<String> openaiApiKey;

    @ConfigProperty(name = "openai.model.name", defaultValue = "gpt-4o-mini")
    String modelName;

    private ChatModel chatModel;
    private EmbeddingModel embeddingModel;
    private Map<String, Evaluator> evaluators;
    private Map<String, MetricInfo> metricInfo;

    @PostConstruct
    void init() {
        evaluators = new ConcurrentHashMap<>();
        metricInfo = new HashMap<>();

        // Algorithmic metrics (no API key needed)
        metricInfo.put("bleu_score", new MetricInfo(
            "bleu_score",
            "BLEU Score",
            "Bilingual Evaluation Understudy - measures n-gram overlap between answer and ground truth. Fast, algorithmic metric.",
            List.of("answer", "groundTruth")
        ));
        evaluators.put("bleu_score", new BleuScoreEvaluator());

        metricInfo.put("rouge_1", new MetricInfo(
            "rouge_1",
            "ROUGE-1",
            "Recall-Oriented Understudy for Gisting Evaluation - measures unigram (single word) overlap. Fast, algorithmic metric.",
            List.of("answer", "groundTruth")
        ));
        evaluators.put("rouge_1", new RougeScoreEvaluator(
            RougeScoreEvaluator.RougeType.ROUGE1,
            RougeScoreEvaluator.MeasureType.F1SCORE
        ));

        metricInfo.put("rouge_2", new MetricInfo(
            "rouge_2",
            "ROUGE-2",
            "Measures bigram (two consecutive words) overlap between answer and ground truth. Fast, algorithmic metric.",
            List.of("answer", "groundTruth")
        ));
        evaluators.put("rouge_2", new RougeScoreEvaluator(
            RougeScoreEvaluator.RougeType.ROUGE2,
            RougeScoreEvaluator.MeasureType.F1SCORE
        ));

        metricInfo.put("rouge_l", new MetricInfo(
            "rouge_l",
            "ROUGE-L",
            "Measures longest common subsequence between answer and ground truth. Captures sentence-level structure. Fast, algorithmic metric.",
            List.of("answer", "groundTruth")
        ));
        evaluators.put("rouge_l", new RougeScoreEvaluator(
            RougeScoreEvaluator.RougeType.ROUGE_L,
            RougeScoreEvaluator.MeasureType.F1SCORE
        ));

        // LLM-based metrics (require API key)
        if (openaiApiKey.isPresent() && !openaiApiKey.get().isBlank()) {
            String apiKey = openaiApiKey.get();

            chatModel = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .temperature(0.0)
                .build();

            embeddingModel = OpenAiEmbeddingModel.builder()
                .apiKey(apiKey)
                .modelName("text-embedding-3-small")
                .build();

            metricInfo.put("answer_correctness", new MetricInfo(
                "answer_correctness",
                "Answer Correctness",
                "LLM-based evaluation of how correct the answer is compared to the ground truth. Measures factual accuracy.",
                List.of("question", "answer", "groundTruth")
            ));
            evaluators.put("answer_correctness", new AnswerCorrectnessEvaluator(chatModel));

            metricInfo.put("answer_relevance", new MetricInfo(
                "answer_relevance",
                "Answer Relevance",
                "LLM-based measurement of how relevant the answer is to the question asked.",
                List.of("question", "answer")
            ));
            evaluators.put("answer_relevance", new AnswerRelevanceEvaluator(chatModel, embeddingModel));

            metricInfo.put("faithfulness", new MetricInfo(
                "faithfulness",
                "Faithfulness",
                "LLM-based evaluation of whether the answer is faithful to the provided context. Detects hallucinations.",
                List.of("question", "answer", "context")
            ));
            evaluators.put("faithfulness", new FaithfulnessEvaluator(chatModel));

            metricInfo.put("semantic_similarity", new MetricInfo(
                "semantic_similarity",
                "Semantic Similarity",
                "Embedding-based semantic similarity between the answer and ground truth using vector comparison.",
                List.of("answer", "groundTruth")
            ));
            evaluators.put("semantic_similarity", new AnswerSemanticSimilarityEvaluator(embeddingModel));
        }
    }

    public boolean isConfigured() {
        return chatModel != null;
    }

    public boolean hasAlgorithmicMetrics() {
        return !evaluators.isEmpty();
    }

    public List<MetricInfo> getAvailableMetrics() {
        return new ArrayList<>(metricInfo.values());
    }

    public List<EvaluationResult> evaluate(EvaluationRequest request) {
        List<EvaluationResult> results = new ArrayList<>();

        if (evaluators.isEmpty()) {
            return List.of(EvaluationResult.error("configuration", "No evaluators available."));
        }

        List<String> metricsToRun = request.getMetrics();
        if (metricsToRun == null || metricsToRun.isEmpty()) {
            metricsToRun = new ArrayList<>(evaluators.keySet());
        }

        for (String metricId : metricsToRun) {
            results.add(evaluateMetric(metricId, request));
        }

        return results;
    }

    private EvaluationResult evaluateMetric(String metricId, EvaluationRequest request) {
        Evaluator evaluator = evaluators.get(metricId);
        MetricInfo info = metricInfo.get(metricId);

        if (evaluator == null || info == null) {
            return EvaluationResult.error(metricId, "Unknown metric: " + metricId);
        }

        try {
            Sample.SampleBuilder sampleBuilder = Sample.builder();

            if (request.getQuestion() != null) {
                sampleBuilder.withQuestion(request.getQuestion());
            }
            if (request.getAnswer() != null) {
                sampleBuilder.withAnswer(request.getAnswer());
            }
            if (request.getGroundTruth() != null) {
                sampleBuilder.withGroundTruth(request.getGroundTruth());
            }
            if (request.getContexts() != null && !request.getContexts().isEmpty()) {
                String combinedContext = String.join("\n\n", request.getContexts());
                sampleBuilder.withContext(combinedContext);
            }

            Sample sample = sampleBuilder.build();

            long startTime = System.currentTimeMillis();
            Evaluation evaluation = evaluator.evaluate(sample);
            long executionTime = System.currentTimeMillis() - startTime;

            return new EvaluationResult(
                info.getName(),
                evaluation.getValue(),
                info.getDescription(),
                executionTime
            );
        } catch (Exception e) {
            return EvaluationResult.error(info.getName(), e.getMessage());
        }
    }
}
