---
title: Examples
sidebar_position: 3
---

### Example: Testing Answer Correctness

``` JAVA
RageAssert rageAssert = new OpenAiLLMBuilder().fromApiKey(key);
rageAssert.given()
    .question(QUESTION)
    .groundTruth(GROUND_TRUTH)
    .when()
    .answer(model.generate(QUESTION))
    .then()
    .assertAnswerCorrectness(0.7);
```

This example demonstrates how to use the [`assertAnswerCorrectness`](/docs/rage4j-core/metrics/answer_correctness)
feature. It checks if the model's generated answer
meets a correctness threshold of 0.7 compared to the defined ground truth.

### Example: Testing Faithfulness

``` java
RageAssert rageAssert = new OpenAiLLMBuilder().fromApiKey(key);
rageAssert.given()
    .question(QUESTION)
    .groundTruth(GROUND_TRUTH)
    .contextList(List.of(ANSWER))
    .when()
    .answer(model::generate)
    .then()
    .assertFaithfulness(0.7);
```

This example illustrates the use of [`assertFaithfulness`](/docs/rage4j-core/metrics/faithfulness), ensuring that the
generated answer adheres to the provided
context and retains at least 0.7 faithfulness compared to the ground truth.

### Example: Testing Semantic Similarity

``` java
RageAssert rageAssert = new OpenAiLLMBuilder().fromApiKey(key);
rageAssert.given()
    .question(QUESTION)
    .groundTruth(GROUND_TRUTH)
    .when()
    .answer(model::generate)
    .then()
    .assertSemanticSimilarity(0.7);
```

In this example, [`assertSemanticSimilarity`](/docs/rage4j-core/metrics/answer_semantic_similarity)  is used to verify
that the
semantic similarity score between the model's
answer and the ground truth is at least 0.7.

### Example: Testing Answer Relevance

``` java
RageAssert rageAssert = new OpenAiLLMBuilder().fromApiKey(key);
rageAssert.given()
    .question(QUESTION)
    .groundTruth(GROUND_TRUTH)
    .contextList(CONTEXT)
    .when()
    .answer(model::generate)
    .then()
    .assertAnswerRelevance(0.7);
```

This example uses the [`assertAnswerRelevance`](/docs/rage4j-core/metrics/answer_relevance) feature, checking that the
model's answer is relevant to the context
provided, with a relevance score of at least 0.7.

### Example: Concatenation of multiple assertions

``` java
    RageAssert rageAssert = new OpenAiLLMBuilder().fromApiKey(key);
    rageAssert.given()
       .question(QUESTION)
       .groundTruth(GROUND_TRUTH)
       .when()
       .answer(model.generate(QUESTION))
       .then()
       .assertAnswerCorrectness(0.7)
       .then()
       .assertSemanticSimilarity(0.7);
```

This example demonstrates how to apply multiple assertions to a single LLM-generated answer.
Assertions can be chained, allowing you to combine different evaluation metrics such as correctness and semantic similarity.
This is the recommended approach for testing one answer against multiple metrics.