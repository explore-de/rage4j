# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Rage4J is a RAG (Retrieval-Augmented Generation) Evaluation library for Java. It provides tools to evaluate and measure the quality of language model outputs using various metrics like correctness, relevance, faithfulness, and semantic similarity.

## Build Commands

```bash
# Build the entire project (from root)
./mvnw clean install

# Run unit tests only (excludes integration tests by default)
./mvnw test

# Run integration tests (requires OPEN_AI_KEY environment variable)
./mvnw test -DincludedGroups=integration -DexcludedGroups=

# Run a single test class
./mvnw test -pl dev.rage4j.rage4j -Dtest=FaithfulnessEvaluatorTest

# Run a single test method
./mvnw test -pl dev.rage4j.rage4j -Dtest=FaithfulnessEvaluatorTest#testMethodName

# Format code
./mvnw formatter:format

# Validate formatting
./mvnw formatter:validate

# Enable detailed metric logs during tests
./mvnw test -Dshow.metric.logs=true
```

## Project Structure

Multi-module Maven project with parent POM `rage4j-reactor`:

- **dev.rage4j.rage4j** - Core evaluation library with evaluators and model classes
- **dev.rage4j.rage4j-assert** - Fluent assertion library for RAG evaluation in tests
- **dev.rage4j.rage4j-persist** - Persistence module for saving evaluation results (JSONL format)
- **dev.rage4j.rage4j-persist-junit5** - JUnit 5 extension for automatic persistence lifecycle

## Architecture

### Core Module (rage4j)

- `Sample` - Input data model with question, answer, groundTruth, and context fields (builder pattern)
- `Evaluator` interface - Contract for all evaluators: `Evaluation evaluate(Sample sample)`
- `Evaluation` - Result with metric name and score value
- `EvaluationAggregator` - Runs multiple evaluators and returns `EvaluationAggregation`

**Available Evaluators:**
- `AnswerCorrectnessEvaluator` - Correctness vs ground truth
- `AnswerRelevanceEvaluator` - Answer relevance to question
- `FaithfulnessEvaluator` - Claims inferable from context
- `AnswerSemanticSimilarityEvaluator` - Embedding-based similarity
- `BleuScoreEvaluator` - BLEU score calculation
- `RougeScoreEvaluator` - ROUGE score calculation

### Assert Module (rage4j-assert)

Fluent API: `rageAssert.given().question().groundTruth().when().answer().then().assertFaithfulness(0.7)`

- `RageAssert` - Entry point created via `OpenAiLLMBuilder`
- `AssertionObserver` - Interface for evaluation callbacks (used by persist module)

### Persist Module (rage4j-persist)

- `EvaluationStore` interface with `JsonLinesStore` implementation
- `PersistingObserver` - Connects rage4j-assert to persistence
- `Rage4jPersist` - Static API for global store configuration

### JUnit 5 Extension (rage4j-persist-junit5)

- `@Rage4jPersistConfig(file = "path")` - Annotation for test classes
- `Rage4jPersistExtension` - Manages store lifecycle and injects `EvaluationStore`

## Key Dependencies

- LangChain4j (v1.0.1) - LLM integration (provided scope)
- JUnit Jupiter 5 - Testing framework
- Java 21 required

## Code Style

Uses Eclipse-style formatter configured in `formatter/java.xml`. Opening braces on new lines. Run `./mvnw formatter:format` before committing.
