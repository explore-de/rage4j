---
title: Core concepts
sidebar_position: 2
---

# Core Concepts

## Sample
The `Sample` class is the fundamental data structure representing an evaluation instance:

```java
Sample sample = Sample.builder()
    .withQuestion("What is the capital of France?")
    .withAnswer("Paris is the capital of France.")
    .withGroundTruth("Paris is the capital and largest city of France.")
    .withContextsList(Arrays.asList("Paris is the capital of France..."))
    .build();
```

A Sample typically consists of:
- A **question**: the prompt or input to the language model.
- An **answer**: the model-generated response.
- A **ground truth**: the expected or correct answer.
- **Contexts** (optional): additional information related to the question.

---

## Evaluators
Each evaluator implements the `Evaluator` interface and focuses on a specific aspect of evaluation:

```java
public interface Evaluator {
    Evaluation evaluate(Sample sample);
}
```

---

## Evaluation
The Evaluation class represents the result of a single metric assessment:

```java
Evaluation result = evaluator.evaluate(sample);
String metricName = result.getName();    // e.g., "Answer correctness"
double score = result.getValue();        // Score between 0 and 1
```

---

## Evaluation Aggregation
Results from multiple evaluators can be combined using the `EvaluationAggregator`:

```java
public class EvaluationAggregator {
  public static EvaluationAggregation evaluateAll(Sample sample, Evaluator... evaluators);
}
```

---

## Example Usage

Here's a complete example demonstrating how to evaluate an LLM response using multiple metrics:

```java
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;

public class EvaluationExample {
    public static void main(String[] args) {
        ChatLanguageModel chatModel = /* Any Langchain4j ChatLanguageModel */
        EmbeddingModel embeddingModel = /* Any Langchain4j EmbeddingModel */

        Evaluator relevanceEvaluator = new AnswerRelevanceEvaluator(chatModel, embeddingModel);
        Evaluator correctnessEvaluator = new AnswerCorrectnessEvaluator(chatModel);
        Evaluator faithfulnessEvaluator = new FaithfulnessEvaluator(chatModel);
        Evaluator similarityEvaluator = new AnswerSemanticSimilarityEvaluator(embeddingModel);

        Sample sample = Sample.builder()
            .withQuestion("What are the main features of Java?")
            .withAnswer("Java is object-oriented, platform-independent, and has automatic memory management.")
            .withGroundTruth("Java's main features include object-oriented programming, platform independence through JVM, automatic memory management (garbage collection), and strong type safety.")
            .withContextsList(Arrays.asList(
                "Java is a popular programming language...",
                "Key features of Java include..."
            ))
            .build();

        EvaluationAggregation results = EvaluationAggregator.evaluateAll(sample,
            relevanceEvaluator,
            correctnessEvaluator,
            faithfulnessEvaluator,
            similarityEvaluator
        );

        // Access results
        System.out.println("Relevance score: " + results.get("Answer relevance"));
        System.out.println("Correctness score: " + results.get("Answer correctness"));
        System.out.println("Faithfulness score: " + results.get("Faithfulness"));
        System.out.println("Semantic similarity: " + results.get("Answer semantic similarity"));
    }
}
```