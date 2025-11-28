# Rage4J Assert

Fluent assertion library for RAG evaluation in tests.

## Overview

The **Rage4J Assert** module provides a fluent API for writing RAG evaluation assertions in your tests. It integrates with Rage4J evaluators and supports automatic recording of results via observers.

## Installation

Add this dependency to your pom.xml:

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
import dev.rage4j.asserts.OpenAiLLMBuilder;

RageAssert rageAssert = new OpenAiLLMBuilder().fromApiKey(apiKey);

rageAssert.given()
    .question("What is the capital of France?")
    .groundTruth("Paris")
    .contextList(List.of("France is a country in Europe.", "Paris is the capital of France."))
    .when()
    .answer("Paris is the capital of France.")
    .then()
    .assertFaithfulness(0.7)
    .assertAnswerCorrectness(0.8)
    .assertAnswerRelevance(0.7);
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

## Available Assertions

| Method | Description |
|--------|-------------|
| `assertFaithfulness(minValue)` | Checks if claims in answer can be inferred from context |
| `assertAnswerCorrectness(minValue)` | Evaluates correctness compared to ground truth |
| `assertAnswerRelevance(minValue)` | Measures relevance of answer to question |
| `assertSemanticSimilarity(minValue)` | Computes semantic similarity to ground truth |
| `assertBleuScore(minValue)` | Calculates BLEU score against ground truth |
| `assertRougeScore(minValue, rougeType, measureType)` | Calculates ROUGE score |

## Observer Pattern

You can attach observers to automatically receive notifications when evaluations complete:

```java
import dev.rage4j.asserts.AssertionObserver;

// Create a custom observer
AssertionObserver observer = (sample, evaluation, passed) -> {
    System.out.println("Evaluation: " + evaluation.getName() + " = " + evaluation.getValue());
};

rageAssert.addObserver(observer);
```

### Integration with rage4j-persist

Use `PersistingObserver` to automatically save evaluation results:

```java
import dev.rage4j.persist.PersistingObserver;
import dev.rage4j.persist.Rage4jPersist;

EvaluationStore store = Rage4jPersist.jsonLines("target/evaluations.jsonl");
rageAssert.addObserver(new PersistingObserver(store));

// All assertions are now automatically recorded!
rageAssert.given()
    .question("What is AI?")
    .groundTruth("...")
    .when()
    .answer(llm::chat)
    .then()
    .assertFaithfulness(0.7);  // Recorded to JSONL file
```

## Key Classes

| Class | Description |
|-------|-------------|
| `RageAssert` | Main entry point for fluent assertions |
| `RageAssertTestCaseBuilder` | Builder for test case setup (given phase) |
| `RageAssertTestCaseGiven` | Builder for answer setup (when phase) |
| `RageAssertTestCaseAssertions` | Assertion methods (then phase) |
| `AssertionObserver` | Interface for evaluation observers |
| `OpenAiLLMBuilder` | Builder for OpenAI-backed RageAssert |
