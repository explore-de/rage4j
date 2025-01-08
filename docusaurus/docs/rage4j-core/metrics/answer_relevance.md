---
title: Answer Relevance
sidebar_position: 2
---

# Answer Relevance

**Evaluator**: `AnswerRelevanceEvaluator`

Measures how well the answer addresses the original question by analyzing the semantic relationship between the question and answer.

## Required Sample fields
- `question` - Required for comparing with generated questions
- `answer` - Required for generating potential questions

## How it works
1. Generates 5 potential questions that the answer could be responding to
2. Compares these generated questions with the original question using embedding-based similarity
3. Calculates the mean similarity score

## Score interpretation
- Range: 0 to 1
- Higher scores indicate the answer better addresses the original question
- A score close to 1 suggests the answer is highly relevant to the question
- A score close to 0 suggests the answer may be off-topic or unrelated

```java
AnswerRelevanceEvaluator evaluator = new AnswerRelevanceEvaluator(chatModel, embeddingModel);
Evaluation result = evaluator.evaluate(sample);
double relevanceScore = result.getValue();
```