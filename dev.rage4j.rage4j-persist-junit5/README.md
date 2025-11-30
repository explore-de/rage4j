# Rage4J Persist JUnit 5

JUnit 5 extension for automatic persistence of Rage4J evaluation results.

## Overview

The Rage4J Persist JUnit 5 module provides a JUnit 5 extension that automatically manages the lifecycle of an `EvaluationStore` during test execution. It handles store creation, parameter injection, and cleanup.

## Installation

Add this dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>dev.rage4j</groupId>
    <artifactId>rage4j-persist-junit5</artifactId>
    <version>1.1.1-SNAPSHOT</version>
    <scope>test</scope>
</dependency>
```

## Usage

### Basic Usage

Annotate your test class with `@Rage4jPersistConfig`:

```java
import dev.rage4j.persist.junit5.Rage4jPersistConfig;
import dev.rage4j.persist.EvaluationStore;

@Rage4jPersistConfig(file = "target/evaluations.jsonl")
class MyEvaluationTest {

    @Test
    void testEvaluation(EvaluationStore store) {
        // store is automatically injected
        Sample sample = Sample.builder()
            .withQuestion("What is AI?")
            .withAnswer("AI is artificial intelligence")
            .withGroundTruth("AI is artificial intelligence")
            .build();

        EvaluationAggregation result = EvaluationAggregator.evaluateAll(sample,
            new FaithfulnessEvaluator(chatModel));

        store.store(result);

        assertThat(result.get("Faithfulness")).isGreaterThan(0.7);
    }
}
```

### With rage4j-assert

Combine with rage4j-assert for fluent assertions and persist the results:

```java
@Rage4jPersistConfig(file = "target/evaluations.jsonl")
class MyAssertTest {

    @Test
    void testWithAssert(EvaluationStore store) {
        RageAssert rageAssert = new OpenAiLLMBuilder().fromApiKey(apiKey);

        EvaluationAggregation result = rageAssert.given()
            .question("What is AI?")
            .groundTruth("Artificial intelligence...")
            .when()
            .answer(llm::chat)
            .then()
            .assertFaithfulness(0.7)
            .getEvaluationAggregation();

        store.store(result);  // Persist to injected store
    }
}
```

## Configuration Options

The `@Rage4jPersistConfig` annotation supports:

| Attribute | Default | Description |
|-----------|---------|-------------|
| `file` | `"target/evaluations.jsonl"` | Path to the output file |
| `storeClass` | `JsonLinesStore.class` | Store implementation class (must have a `Path` constructor) |

### Custom Store Implementation

You can provide a custom store implementation:

```java
@Rage4jPersistConfig(file = "target/custom.dat", storeClass = MyCustomStore.class)
class MyCustomTest {
    // MyCustomStore must have a public constructor that accepts a Path
}
```

## Features

- **Automatic Lifecycle Management**: Store is created before tests and closed after
- **Parameter Injection**: Inject `EvaluationStore` into test methods
- **Inheritance Support**: Configuration is inherited from parent test classes
