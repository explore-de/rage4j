---
title: Answer Semantic Similarity
sidebar_position: 5
---

# Answer Semantic Similarity

**Evaluator**: `AnswerSemanticSimilarityEvaluator`

Computes the semantic similarity between the answer and the ground truth using embedding-based comparison.

## Required Sample fields
- `answer` - Required for semantic comparison
- `groundTruth` - Required for semantic comparison

## How it works
1. Generates embeddings for both the answer and ground truth
2. Calculates the cosine similarity between these embeddings
3. Returns the similarity score directly

## Score interpretation
- Range: 0 to 1
- Based on cosine similarity between embeddings
- Higher scores indicate closer semantic meaning
- Perfect score (1.0) suggests nearly identical semantic content
- Scores above 0.8 typically indicate very similar meaning

```java
AnswerSemanticSimilarityEvaluator evaluator = new AnswerSemanticSimilarityEvaluator(embeddingModel);
Evaluation result = evaluator.evaluate(sample);
double similarityScore = result.getValue();
```