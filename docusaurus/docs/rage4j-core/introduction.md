---
title: Introduction
sidebar_position: 1
---

# Introduction

**RAGE4j-core** is a Java library for evaluating Large Language Model (LLM) generations, inspired by the Python Ragas
library. It provides a robust framework for assessing the quality of LLM responses across multiple dimensions.

## Overview

The RAGE4j library consists of two main components:

- **RAGE4j-Core**: The core library providing evaluation tools and metrics
- **RAGE4j-Assert**: An extension offering a testing API for LLM evaluations

This section provides documentation about the **RAGE4j-core**.

## Key Features

RAGE4j provides six built-in evaluators for assessing different aspects of LLM responses:

1. **Answer Relevance**: Evaluates how well an answer addresses the original question by generating potential questions
   from the answer and comparing them to the original question.

2. **Answer Correctness**: Measures the accuracy of an answer against ground truth using an F1 score based on true
   positive, false positive, and false negative claims.

3. **Faithfulness**: Assesses whether the claims in an answer can be supported by the provided context.

4. **Semantic Similarity**: Computes the semantic similarity between the answer and ground truth using embedding-based
   cosine similarity.
5. **Bleu score**: Computes the n-gram overlap precision between a ground truth and an LLM response.
6. **Rouge Score**: Computes precision, recall or F1 score for unigram, bigram or LCS (Longest common subsequence)
   overlap
   between
   a ground truth and an LLM response.

## Quick example

```java
// 1. Create an evaluator
Evaluator answerRelevanceEvaluator = new AnswerRelevanceEvaluator(chatModel, embeddingModel);

// 2. Create a sample
Sample sample = Sample.builder()
	.withQuestion("What is Java?")
	.withAnswer("Java is a programming language.")
	.build();

// 3. Evaluate and get results
Evaluation result = answerRelevanceEvaluator.evaluate(sample);

// 4. Get our score
System.out.

println("Metric name: "+result.getName()); // Metric name: Answer relevance
	System.out.

println("Metric score: "+result.getName()); // Metric score: 1.0
```
