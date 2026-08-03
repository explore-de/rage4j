---
id: introduction
title: Quarkus integration
sidebar_position: 1
---

# Rage4J Quarkus

The `rage4j-quarkus` extension exposes Rage4J as a CDI-friendly test dependency. It reuses the `ChatModel` and optional `EmbeddingModel` already managed by Quarkus or by application producers.

## Installation

```xml
<dependency>
    <groupId>dev.rage4j</groupId>
    <artifactId>rage4j-quarkus</artifactId>
    <version>2.0.2-SNAPSHOT</version>
    <scope>test</scope>
</dependency>
```

## Inject `RageAssert`

```java
@QuarkusTest
class LoraEvaluationTest
{
    @Inject
    RageAssert rageAssert;

    @Test
    void evaluatesAnswer()
    {
        rageAssert.given()
            .question("What is the capital of France?")
            .groundTruth("Paris")
            .when()
            .answer("Paris")
            .then()
            .assertAnswerCorrectness(0.8);
    }
}
```

The extension deliberately does not create another provider client or duplicate API-key configuration. It consumes CDI-managed LangChain4j model beans, which keeps the test model aligned with the application model.

## Configuration

```properties
quarkus.rage4j.evaluation-mode=false
quarkus.rage4j.persistence.enabled=false
quarkus.rage4j.persistence.file=target/evaluations.jsonl
```

When persistence is enabled, inject `EvaluationStore`, obtain the aggregation from the assertion chain, and call `storeFlush(...)`. If multiple model beans are available, select one with `quarkus.rage4j.chat-model-bean` or `quarkus.rage4j.embedding-model-bean`; values may be CDI bean names, bean class names, or `@ModelName` values.
