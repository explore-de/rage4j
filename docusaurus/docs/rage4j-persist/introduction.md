---
title: Introduction
sidebar_position: 2
---

# Introduction

RAGE4J-Persist provides persistence capabilities for saving evaluation results to files. This enables tracking evaluation results over time and integrating with data analysis tools.

## EvaluationStore Interface

The `EvaluationStore` interface defines how evaluation results are persisted:

```java
public interface EvaluationStore extends Closeable {
    void store(EvaluationAggregation aggregation);  // Buffer data
    void flush();                                    // Write to storage
    void storeFlush(EvaluationAggregation aggregation); // Store and flush
    void close();                                   // Release resources
}
```

## JsonLinesStore

The primary implementation stores evaluations in JSON Lines format (`.jsonl`), where each line is a complete JSON object:

```java
try (EvaluationStore store = new JsonLinesStore(Path.of("target/evaluations.jsonl"))) {
    EvaluationAggregation result = EvaluationAggregator.evaluateAll(sample, evaluators);
    store.store(result);
} // flush() called automatically on close()
```

Output format:
```json
{"sample":{"question":"...","answer":"...","groundTruth":"..."},"metrics":{"Answer correctness":0.85}}
```

## CompositeStore

Write to multiple stores simultaneously:

```java
EvaluationStore composite = new CompositeStore(
    new JsonLinesStore(Path.of("results.jsonl")),
    new JsonLinesStore(Path.of("backup.jsonl"))
);
```

## Key Features

- **Buffered writes**: Data is buffered until `flush()` or `close()` is called
- **File locking**: Thread-safe writes with file-level locking
- **Auto-close**: Implements `Closeable` for try-with-resources
- **Extensible**: Implement `EvaluationStore` for custom backends
