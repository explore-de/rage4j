# Rage4J Assert

Fluent assertion library for RAG evaluation in tests.

## Overview

The Rage4J Assert module provides a fluent API for writing RAG evaluation assertions in your tests. It integrates with Rage4J evaluators and returns evaluation results that can be used for persistence or further analysis.

## Installation

Add this dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>dev.rage4j</groupId>
    <artifactId>rage-assert</artifactId>
    <version>1.1.1-SNAPSHOT</version>
    <scope>test</scope>
</dependency>
```

## Usage

### Basic Usage

```java
import dev.rage4j.asserts.RageAssert;
import dev.rage4j.asserts.openai.OpenAiLLMBuilder;

RageAssert rageAssert = new OpenAiLLMBuilder().fromApiKey(apiKey);

rageAssert.given()
    .question("What is the capital of France?")
    .groundTruth("Paris")
    .context("France is a country in Europe. Paris is the capital of France.")
    .when()
    .answer("Paris is the capital of France.")
    .then()
    .assertFaithfulness(0.7)
    .then()
    .assertAnswerCorrectness(0.8)
    .then()
    .assertAnswerRelevance(0.7);
```

### Custom Model Selection

```java
RageAssert rageAssert = new OpenAiLLMBuilder()
    .withChatModel("gpt-4o")
    .withEmbeddingModel("text-embedding-3-large")
    .fromApiKey(apiKey);
```

### With LLM Integration

```java
rageAssert.given()
    .question("What is AI?")
    .groundTruth("Artificial intelligence is...")
    .when()
    .answer(question -> llm.generate(question))  // Call your LLM
    .then()
    .assertFaithfulness(0.7);
```

### Evaluation Mode

By default, assertion failures throw exceptions. Use **evaluation mode** to log warnings instead, allowing complete evaluation runs for data collection:

```java
RageAssert rageAssert = new OpenAiLLMBuilder()
    .fromApiKey(apiKey)
    .withEvaluationMode();  // Failures log warnings instead of throwing

rageAssert.given()
    .question("What is AI?")
    .groundTruth("...")
    .when()
    .answer(llm::chat)
    .then()
    .assertFaithfulness(0.7)   // Logs warning if below threshold
    .then()
    .assertAnswerCorrectness(0.8);  // Still runs even if previous failed
```

Switch back to strict mode:

```java
rageAssert.withStrictMode();  // Failures throw exceptions again
```

### Getting Evaluation Results

Use `getEvaluationAggregation()` to retrieve the collected metrics:

```java
import dev.rage4j.model.EvaluationAggregation;

EvaluationAggregation result = rageAssert.given()
    .question("What is AI?")
    .groundTruth("...")
    .when()
    .answer(llm::chat)
    .then()
    .assertFaithfulness(0.7)
    .then()
    .assertAnswerCorrectness(0.8)
    .getEvaluationAggregation();  // Returns aggregation with sample and all metrics

// Access the metrics
result.forEach((metric, score) -> System.out.println(metric + ": " + score));

// Access the sample
result.getSample().ifPresent(sample -> System.out.println(sample.getQuestion()));
```

### Persisting Results

Use the rage4j-persist module to persist evaluation results:

```java
import dev.rage4j.persist.store.JsonLinesStore;
import java.nio.file.Path;

EvaluationAggregation result = rageAssert.given()
    .question("What is AI?")
    .groundTruth("...")
    .when()
    .answer(llm::chat)
    .then()
    .assertFaithfulness(0.7)
    .getEvaluationAggregation();

// Persist manually
try (JsonLinesStore store = new JsonLinesStore(Path.of("target/evaluations.jsonl"))) {
    store.store(result);
}  // flush() is called automatically on close()
```

## Available Assertions

| Method | Description |
|--------|-------------|
| `assertFaithfulness(minValue)` | Checks if claims in answer can be inferred from context |
| `assertAnswerCorrectness(minValue)` | Evaluates correctness compared to ground truth |
| `assertAnswerRelevance(minValue)` | Measures relevance of answer to question |
| `assertSemanticSimilarity(minValue)` | Computes semantic similarity to ground truth |
| `assertBleuScore(minValue)` | Calculates BLEU score against ground truth |
| `assertRougeScore(minValue, rougeType, measureType)` | Calculates ROUGE score |

Each assertion returns an `AssertionEvaluation` that provides access to the raw `Evaluation` result and allows chaining via `.then()`.

## Key Classes

| Class | Description |
|-------|-------------|
| `RageAssert` | Main entry point for fluent assertions |
| `RageAssertTestCaseBuilder` | Builder for test case setup (given phase) |
| `RageAssertTestCaseGiven` | Builder for answer setup (when phase) |
| `RageAssertTestCaseAssertions` | Assertion methods (then phase) |
| `AssertionEvaluation` | Result wrapper enabling assertion chaining via `.then()` |
| `OpenAiLLMBuilder` | Builder for OpenAI-backed RageAssert |
