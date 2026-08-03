# Rage4J Quarkus

The Rage4J Quarkus extension exposes Quarkus-managed LangChain4j models as a CDI-managed `RageAssert` for `@QuarkusTest` classes.

## Installation

```xml
<dependency>
    <groupId>dev.rage4j</groupId>
    <artifactId>rage4j-quarkus</artifactId>
    <version>2.0.2-SNAPSHOT</version>
    <scope>test</scope>
</dependency>
```

The application must provide a CDI `dev.langchain4j.model.chat.ChatModel`. An `EmbeddingModel` is optional, but is required by semantic-similarity and embedding-based assertions.

## Usage

```java
@QuarkusTest
class LoraEvaluationTest
{
    @Inject
    RageAssert rageAssert;

    @Test
    void evaluatesTheAnswer()
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

The extension does not create provider clients or read provider credentials. It reuses the models already configured by Quarkus LangChain4j or by application CDI producers.

## Configuration

```properties
quarkus.rage4j.evaluation-mode=false
quarkus.rage4j.persistence.enabled=false
quarkus.rage4j.persistence.file=target/evaluations.jsonl
```

When persistence is enabled, inject `EvaluationStore` and store an `EvaluationAggregation` explicitly:

```java
@Inject
EvaluationStore evaluationStore;

EvaluationAggregation result = rageAssert.given()
    .question("What is the capital of France?")
    .groundTruth("Paris")
    .when()
    .answer("Paris")
    .then()
    .assertAnswerCorrectness(0.8)
    .getEvaluationAggregation();

evaluationStore.storeFlush(result);
```

If more than one model bean is available, select one with `quarkus.rage4j.chat-model-bean` or `quarkus.rage4j.embedding-model-bean`. The value may be a CDI bean name, bean class name, or the value of Quarkus LangChain4j's `@ModelName` qualifier.
