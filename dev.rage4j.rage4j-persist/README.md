# Rage4J Persist

Persistence module for saving Rage4J evaluation results to files.

## Overview

The **Rage4J Persist** module provides tools to persist evaluation results from Rage4J evaluators. It supports JSON Lines (JSONL) format for append-friendly, structured storage of evaluation data.

## Installation

Add this dependency to your pom.xml:

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
import dev.rage4j.persist.Rage4jPersist;
import dev.rage4j.persist.EvaluationStore;

// Create a JSONL store
EvaluationStore store = Rage4jPersist.jsonLines("target/evaluations.jsonl");

// Store evaluation results
EvaluationAggregation result = EvaluationAggregator.evaluateAll(sample, evaluators);
store.store(result);

// Close when done
store.close();
```

### Static API

For convenience, you can configure a global store and use the static API:

```java
// Configure once at startup
Rage4jPersist.configure(Rage4jPersist.jsonLines("target/evaluations.jsonl"));

// Record anywhere in your code
EvaluationAggregation result = EvaluationAggregator.evaluateAll(sample, evaluators);
Rage4jPersist.store(result);
```

### With Metadata

You can attach test metadata to records:

```java
import dev.rage4j.persist.RecordMetadata;

RecordMetadata metadata = RecordMetadata.of("MyTestClass", "testMethod");
store.store(aggregation, metadata);
```

### Integration with rage4j-assert

Use the `PersistingObserver` to automatically record evaluations from rage4j-assert:

```java
import dev.rage4j.persist.PersistingObserver;

EvaluationStore store = Rage4jPersist.jsonLines("target/evaluations.jsonl");
RageAssert rageAssert = new OpenAiLLMBuilder().fromApiKey(key);
rageAssert.addObserver(new PersistingObserver(store));

// Evaluations are automatically recorded!
rageAssert.given()
    .question("What is AI?")
    .groundTruth("Artificial intelligence...")
    .when()
    .answer(llm::chat)
    .then()
    .assertFaithfulness(0.7);
```

## Output Format

Records are stored in JSON Lines format (one JSON object per line):

```json
{"id":"uuid","timestamp":"2024-01-15T10:30:00Z","testClass":"MyTest","testMethod":"testEval","sample":{"question":"What is AI?","answer":"AI is...","groundTruth":"..."},"metrics":{"Faithfulness":0.85,"BLEU score":0.72}}
```

## Key Classes

| Class | Description |
|-------|-------------|
| `EvaluationStore` | Interface for storing evaluations |
| `JsonLinesStore` | JSONL file implementation |
| `CompositeStore` | Fan-out to multiple stores |
| `Rage4jPersist` | Entry point with factory methods |
| `RecordMetadata` | Test context metadata (class, method, timestamp) |
| `PersistingObserver` | Observer for rage4j-assert integration |
