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

<!---
TODO: add new metrics
-->