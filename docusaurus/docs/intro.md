---
id: intro
title: Getting Started
sidebar_position: 1
slug: /intro
---

# Getting Started

Welcome to **RAGE4J** (**RAG** **E**valuations **f**or **J**ava) - Your Java toolkit for evaluating LLM outputs! 🎉

## What is RAGE4J?

RAGE4J is a Java library suite for evaluating Large Language Model (LLM) outputs. It consists of two main components:

- **RAGE4J-Core**: The foundation library providing evaluation metrics and tools
- **RAGE4J-Assert**: Testing extensions for integrating LLM evaluations into your test suite

## Core Features

RAGE4J helps you assess LLM outputs across four key dimensions:

- **Correctness**: Measures factual accuracy by comparing claims in the LLM output against a ground truth
- **Relevance**: Evaluates if the response actually answers the question asked
- **Faithfulness**: Checks if the LLM's statements are supported by the provided context
- **Semantic Similarity**: Computes how closely the meaning matches a reference answer

## Library Structure

- **RAGE4J-Core**
    - Evaluation metrics
    - Sample handling
    - Result aggregation
    - Utility functions

- **RAGE4J-Assert**
    - JUnit integration
    - Assertion helpers
    - Test utilities

---

Explore more about RAGE4J:

1. [RAGE4j-Core](/docs/category/rage4j-core)
2. [RAGE4j-Assert](/docs/category/rage4j-assert)
3. [Contribution guide](/docs/contribution)