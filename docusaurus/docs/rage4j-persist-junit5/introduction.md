---
title: Introduction
sidebar_position: 2
---

# Introduction

RAGE4J-Persist-JUnit5 provides a JUnit 5 extension that automatically manages the lifecycle of an `EvaluationStore` during test execution.

## @Rage4jPersistConfig Annotation

Annotate your test class to enable automatic persistence:

```java
@Rage4jPersistConfig(file = "target/evaluations.jsonl")
class MyEvaluationTest {

    @Test
    void testEvaluation(EvaluationStore store) {
        // store is injected and ready to use
        EvaluationAggregation aggregation = EvaluationAggregator.evaluateAll(sample, evaluators);
        store.store(aggregation);
    }
}
```

## Annotation Attributes

| Attribute | Default | Description |
|-----------|---------|-------------|
| `file` | `"target/evaluations.jsonl"` | Output file path |
| `storeClass` | `JsonLinesStore.class` | Store implementation class |

## Custom Store Implementation

Use a custom store by specifying the `storeClass` attribute:

```java
@Rage4jPersistConfig(file = "target/custom.dat", storeClass = MyCustomStore.class)
class MyCustomTest {
    // MyCustomStore must have a constructor that accepts a Path
}
```

## Lifecycle

The extension manages the store lifecycle:

1. **BeforeAll**: Creates the `EvaluationStore` instance
2. **Test methods**: Injects `EvaluationStore` via parameter resolution
3. **AfterAll**: Closes the store (flushing any buffered data)

## Complete Example

```java
import dev.rage4j.persist.EvaluationStore;
import dev.rage4j.persist.junit5.Rage4jPersistConfig;
import dev.rage4j.asserts.RageAssert;
import dev.rage4j.asserts.openai.OpenAiLLMBuilder;
import dev.rage4j.model.EvaluationAggregation;
import org.junit.jupiter.api.Test;

@Rage4jPersistConfig(file = "target/my-evaluations.jsonl")
class RagEvaluationTest {

    private final String apiKey = System.getenv("OPEN_AI_KEY");
    private final RageAssert rageAssert = new OpenAiLLMBuilder().fromApiKey(apiKey);

    @Test
    void testCorrectness(EvaluationStore store) {
        EvaluationAggregation result = rageAssert.given()
            .question("What is the capital of France?")
            .groundTruth("Paris")
            .when()
            .answer("Paris is the capital of France.")
            .then()
            .assertAnswerCorrectness(0.7)
            .getEvaluationAggregation();

        store.store(result);
    }
}
```
