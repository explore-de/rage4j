# Rage4J Persist JUnit 5

JUnit 5 extension for automatic persistence of Rage4J evaluation results.

## Overview

The **Rage4J Persist JUnit 5** module provides a JUnit 5 extension that automatically manages the lifecycle of an `EvaluationStore` during test execution. It handles store creation, test context metadata, and cleanup.

## Installation

Add this dependency to your pom.xml:

```xml
<dependency>
    <groupId>dev.rage4j</groupId>
    <artifactId>rage4j-persist-junit5</artifactId>
    <version>1.1.1-SNAPSHOT</version>
    <scope>test</scope>
</dependency>
```

## Usage

### Basic Usage with Annotation

Simply annotate your test class with `@Rage4jPersistConfig`:

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

### Using Static API

The extension also configures the global static store:

```java
@Rage4jPersistConfig(file = "target/evaluations.jsonl")
class MyEvaluationTest {

    @Test
    void testWithStaticApi() {
        EvaluationAggregation result = EvaluationAggregator.evaluateAll(sample, evaluators);

        // Use static API - no parameter injection needed
        Rage4jPersist.store(result);
    }
}
```

### With rage4j-assert

Combine with rage4j-assert for automatic recording:

```java
@Rage4jPersistConfig(file = "target/evaluations.jsonl")
class MyAssertTest {

    RageAssert rageAssert;

    @BeforeEach
    void setUp() {
        rageAssert = new OpenAiLLMBuilder().fromApiKey(key);
        rageAssert.addObserver(new PersistingObserver(Rage4jPersist.configured()));
    }

    @Test
    void testWithAssert() {
        rageAssert.given()
            .question("What is AI?")
            .groundTruth("Artificial intelligence...")
            .when()
            .answer(llm::chat)
            .then()
            .assertFaithfulness(0.7);  // Automatically recorded!
    }
}
```

## Configuration Options

The `@Rage4jPersistConfig` annotation supports:

| Attribute | Default | Description |
|-----------|---------|-------------|
| `file` | `"target/evaluations.jsonl"` | Path to the output file |
| `storeClass` | `JsonLinesStore.class` | Store implementation class (must have a `Path` constructor) |
| `configureGlobal` | `true` | Whether to configure the static `Rage4jPersist` API |

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
- **Test Context Metadata**: Each record includes test class and method names
- **Static API Integration**: Optionally configures global store for `Rage4jPersist.store()`

## How It Works

1. **BeforeAll**: Creates the `EvaluationStore` and optionally configures the global static API
2. **BeforeEach**: Sets test context metadata (class name, method name)
3. **Test Execution**: Store is available via parameter injection or static API
4. **AfterAll**: Closes the store and clears configuration
