---
title: Overview
sidebar_position: 1
---

# Metrics overview

RAGE4j-Core provides four primary metrics for evaluating LLM responses. Each metric focuses on a different aspect of
response quality and is implemented through a dedicated evaluator.

## Basic understanding of the metrics

- **Answer Relevance** (0-1): Higher scores indicate the answer better addresses the original question.
- **Answer Correctness** (0-1): F1 score where 1 indicates perfect alignment with ground truth.
- **Faithfulness** (0-1): Proportion of answer claims that can be supported by the context.
- **Semantic Similarity** (0-1): Cosine similarity between answer and ground truth embeddings.
- **Bleu score** (0-1): Precision-based metric. Measures exact n-gram overlap between the answer and ground truth.
  Higher values indicate more
  literal matching.
- **Rouge score** (0-1): Measures overlap between the answer and reference using multiple variants:
    - **ROUGE-1** (unigrams): Measures key word coverage.
    - **ROUGE-2** (bigrams): Captures word order and fluency.
    - **ROUGE-L**: Longest common subsequence (LCS) at the sentence level.
    - **ROUGE-Lsum**: LCS across sentence pairs (summary-level).
      Each variant provides **Precision**, **Recall**, and **F1** scores.
