# Rage4J

RAG Evaluation library for Java.

## Overview

The **Rage4J Evaluation Library** provides tools to evaluate and measure the quality of language model outputs. It
supports various evaluation metrics to assess different aspects of generated responses, such as correctness, relevance,
faithfulness, and semantic similarity.

The library is modular and allows users to create custom evaluators, integrate with language models and embedding
models, and compute evaluation scores for specific tasks. It is designed to be flexible and easy to integrate into
AI-based projects.

## Installation

Add this dependency to your pom.xml:

```xml

<dependency>
    <groupId>de.explore</groupId>
    <artifactId>rage4j</artifactId>
    <version>1.0.1</version>
</dependency>
```

## Debugging

To enable detailed logs for metric calculations during test execution, you can add the following parameter:

```maven
mvn test -Dshow.metric.logs=true
```

---

## Key Components

### 1. **Sample**

The `Sample` class represents a unit of data to be evaluated. It typically consists of:

- A **question**: the prompt or input to the language model.
- An **answer**: the model-generated response.
- A **ground truth**: the expected or correct answer.
- **Contexts** (optional): additional information related to the question.

Each `Sample` is used as the primary input for the evaluators in this library.

#### Example:

```java
Sample sample = Sample.builder()
	.withQuestion("What is the capital of France?")
	.withAnswer("Paris is the capital of France.")
	.withGroundTruth("Paris")
	.build();
```

### 2. **Evaluators**

The core of the library revolves around **evaluators**. These are classes that assess the quality of a model's output
based on specific metrics. Each evaluator implements the `Evaluator` interface and provides a concrete evaluation method
based on the sample input.

#### Available Evaluators:

- **AnswerCorrectnessEvaluator**: Evaluates the correctness of the answer compared to the ground truth.
- **AnswerRelevanceEvaluator**: Measures the relevance of the answer by comparing generated questions from the answer to
  the original question.
- **FaithfulnessEvaluator**: Checks whether the claims made in the answer can be inferred from the provided context.
- **AnswerSemanticSimilarityEvaluator**: Computes the semantic similarity between the model's answer and the ground
  truth using embeddings.

### 3. **Evaluation**

The `Evaluation` class represents the result of an evaluation and includes:

- The **metric name**: A string representing the evaluated metric (e.g., "Answer correctness").
- The **score**: A `double` representing the computed evaluation score (e.g., an F1 score for correctness).

---

## How to Work with Evaluators

### 1. **Creating an Evaluator**

Each evaluator can be created by passing in the necessary model(s) or services. For instance,
the `AnswerCorrectnessEvaluator` requires a language model to create the bot that will assess the answer’s correctness.

```java
import dev.langchain4j.model.chat.ChatLanguageModel;

ChatLanguageModel chatModel = new ChatLanguageModel(/* Initialize your language model */);
AnswerCorrectnessEvaluator evaluator = new AnswerCorrectnessEvaluator(chatModel);
```

### 2. **Evaluating a Sample**

To evaluate a sample, call the `evaluate` method on an evaluator instance, passing in a `Sample`. The method returns
an `Evaluation` object with the metric name and the calculated score.

```java
Evaluation evaluation = evaluator.evaluate(sample);
System.out.

println("Metric: "+evaluation.getName() +", Score: "+evaluation.

getValue());
```

### 3. **Aggregating Results**

If you want to evaluate a sample using multiple metrics at once, you can use the `EvaluationAggregator`. It takes a
sample and a list of evaluators, computes the evaluations, and aggregates the results.

```java
EvaluationAggregation results = EvaluationAggregator.evaluateAll(sample, evaluator1, evaluator2);
for(
Map.Entry<String, Double> entry :results.

entrySet()){
	System.out.

println(entry.getKey() +": "+entry.

getValue();
}
```

