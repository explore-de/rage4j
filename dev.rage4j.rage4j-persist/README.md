# Rage4J Persist

Persistence module for saving Rage4J evaluation results to files.

## Overview

The Rage4J Persist module provides tools to persist evaluation results from Rage4J evaluators. It supports JSON Lines (JSONL) format for append-friendly, structured storage of evaluation data.

## Installation

Add this dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>dev.rage4j</groupId>
    <artifactId>rage4j-persist</artifactId>
    <version>1.1.1-SNAPSHOT</version>
</dependency>
```

## Usage

### Basic Usage

```java
import dev.rage4j.persist.store.JsonLinesStore;
import java.nio.file.Path;

// Create a JSONL store
JsonLinesStore store = new JsonLinesStore(Path.of("target/evaluations.jsonl"));

// Store evaluation results (buffered)
EvaluationAggregation result = EvaluationAggregator.evaluateAll(sample, evaluators);
store.store(result);

// Flush to disk and close
store.close();  // flush() is called automatically
```

### Buffering and Flushing

The `JsonLinesStore` buffers evaluations in memory and writes them to disk only when `flush()` is called. This allows efficient batch writes:

```java
JsonLinesStore store = new JsonLinesStore(Path.of("target/evaluations.jsonl"));

// Multiple stores are buffered
store.store(aggregation1);
store.store(aggregation2);
store.store(aggregation3);

// Write all buffered data to file
store.flush();

// Or use storeFlush() for immediate write
store.storeFlush(aggregation4);

// close() automatically flushes remaining buffer
store.close();
```

### Composite Store

Write to multiple destinations simultaneously:

```java
import dev.rage4j.persist.store.CompositeStore;
import dev.rage4j.persist.store.JsonLinesStore;

EvaluationStore store = new CompositeStore(
    new JsonLinesStore(Path.of("target/results.jsonl")),
    new JsonLinesStore(Path.of("target/backup.jsonl"))
);

store.store(aggregation);
store.close();
```

### Integration with rage4j-assert

Get evaluation results from rage4j-assert and persist them:

```java
import dev.rage4j.model.EvaluationAggregation;
import dev.rage4j.persist.store.JsonLinesStore;

// Get results from assertions
EvaluationAggregation result = rageAssert.given()
    .question("What is AI?")
    .groundTruth("Artificial intelligence...")
    .when()
    .answer(llm::chat)
    .then()
    .assertFaithfulness(0.7)
    .then()
    .assertAnswerCorrectness(0.8)
    .getEvaluationAggregation();

// Persist the results
try (JsonLinesStore store = new JsonLinesStore(Path.of("target/evaluations.jsonl"))) {
    store.store(result);
}
```

## Output Format

Records are stored in JSON Lines format (one JSON object per line):

```json
{"sample":{"question":"What is AI?","answer":"AI is...","groundTruth":"..."},"metrics":{"Faithfulness":0.85,"Answer correctness":0.72}}
```

## Key Classes

| Class | Description |
|-------|-------------|
| `EvaluationStore` | Interface for storing evaluations |
| `JsonLinesStore` | JSONL file implementation with buffering |
| `CompositeStore` | Fan-out to multiple stores |
