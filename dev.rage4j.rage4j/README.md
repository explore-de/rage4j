# Rage4J Core

Core evaluation library for RAG (Retrieval-Augmented Generation) pipelines.

## Overview

The Rage4J core module provides evaluators to measure the quality of language model outputs. It supports metrics like correctness, relevance, faithfulness, and semantic similarity.

## Installation

Add this dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>dev.rage4j</groupId>
    <artifactId>rage4j</artifactId>
    <version>1.1.1-SNAPSHOT</version>
</dependency>
```

Requires a LangChain4j model implementation (e.g., `langchain4j-open-ai`).

## Usage

### Creating a Sample

```java
Sample sample = Sample.builder()
    .withQuestion("What is the capital of France?")
    .withAnswer("Paris is the capital of France.")
    .withGroundTruth("Paris")
    .withContext("France is a country in Europe. Paris is its capital.")
    .build();
```

### Evaluating with a Single Evaluator

```java
ChatModel chatModel = OpenAiChatModel.builder()
    .apiKey(apiKey)
    .modelName("gpt-4o")
    .build();

Evaluator evaluator = new AnswerCorrectnessEvaluator(chatModel);
Evaluation result = evaluator.evaluate(sample);

System.out.println(result.getName() + ": " + result.getValue());
```

### Aggregating Multiple Evaluators

```java
EvaluationAggregation results = EvaluationAggregator.evaluateAll(sample,
    new AnswerCorrectnessEvaluator(chatModel),
    new FaithfulnessEvaluator(chatModel),
    new BleuScoreEvaluator());

results.forEach((name, score) -> System.out.println(name + ": " + score));
```

## Available Evaluators

| Evaluator | Description | Requires |
|-----------|-------------|----------|
| `AnswerCorrectnessEvaluator` | Evaluates correctness vs ground truth | ChatModel |
| `AnswerRelevanceEvaluator` | Measures relevance of answer to question | ChatModel, EmbeddingModel |
| `FaithfulnessEvaluator` | Checks if claims are inferable from context | ChatModel |
| `AnswerSemanticSimilarityEvaluator` | Embedding-based similarity to ground truth | EmbeddingModel |
| `BleuScoreEvaluator` | BLEU score calculation | None |
| `RougeScoreEvaluator` | ROUGE score calculation | None |

## Debugging

Enable detailed metric logs during tests:

```bash
./mvnw test -Dshow.metric.logs=true
```

