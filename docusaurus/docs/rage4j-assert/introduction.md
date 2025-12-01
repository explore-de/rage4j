---
title: Introduction
sidebar_position: 2
---

# Introduction

If you've recently explored the RAGE4J-Core documentation, you might be wondering: what exactly is RAGE4J-Assert?  
RAGE4J-Assert serves as an intuitive and user-friendly wrapper for the RAGE4J-Core API. During our initial experiences
writing tests with RAGE4J-Core, we noticed that the API could be quite cumbersome, even for crafting simple tests. Our
goal with RAGE4J-Assert is to elevate the developer experience without sacrificing the power of the framework. With
RAGE4J-Assert, writing tests becomes both quicker and more comfortable.

## Simple Example

``` java
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.rage4j.asserts.RageAssert;
import dev.rage4j.asserts.openai.OpenAiLLMBuilder;
import org.junit.jupiter.api.Test;

import static dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O_MINI;

class RageAssertTest
{
	private final String key = System.getenv("OPEN_API_KEY");
	private final OpenAiChatModel model = OpenAiChatModel.builder()
		.apiKey(key)
		.modelName(GPT_4_O_MINI)
		.build();

	@Test
	void testCorrectnessApi()
	{
		RageAssert rageAssert = new OpenAiLLMBuilder().fromApiKey(key);
		rageAssert.given()
			.question("What is the capital of France?")
			.groundTruth("Paris is the capital of France")
			.when()
			.answer(q -> model.generate(q))
			.then()
			.assertAnswerCorrectness(0.7);
	}
}
```

## Model Selection

Configure custom models for chat and embedding operations:

``` java
RageAssert rageAssert = new OpenAiLLMBuilder()
    .withChatModel("gpt-4o")
    .withEmbeddingModel("text-embedding-3-large")
    .fromApiKey(key);
```

Default models: `gpt-5.1` (chat), `text-embedding-3-small` (embedding).

## Evaluation Mode

By default, assertions throw exceptions on failure (**strict mode**). Enable **evaluation mode** to log warnings instead, allowing complete test runs for data collection:

``` java
RageAssert rageAssert = new OpenAiLLMBuilder()
    .fromApiKey(key)
    .withEvaluationMode();  // Logs warnings instead of throwing

// Switch back to strict mode if needed
rageAssert.withStrictMode();
```

## Features

You just saw the `assertAnswerCorrectness` feature in the example above. But that's not all — RAGE4J-Assert supports
additional features from the core API, including:

- `assertFaithfulness(double minValue)`
- `assertAnswerCorrectness(double minValue)`
- `assertAnswerRelevance(double minValue)`
- `assertSemanticSimilarity(double minValue)`
- `asserBleuScore(double minValue)`
- `assertRougeScore(double minValue, RougeScoreEvaluator.RougeType rougeType, RougeScoreEvaluator.MeasureType measureType)`
If you're eager to explore more examples, check out the [examples on the next page](/docs/rage4j-assert/examples)! 😊