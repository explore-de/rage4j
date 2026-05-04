---
title: Faithfulness
sidebar_position: 4
---

# Faithfulness

**Evaluator**: `FaithfulnessEvaluator`

Measures how well the answer's claims are supported by the provided context.

## Required Sample fields
- `answer` - Required for claim extraction
- `contextsList` - Required for verifying claims

## How it works
1. Extracts individual claims from the answer
2. For each claim, checks if it can be inferred from the given context
3. Calculates the proportion of claims that are supported by the context

## Score interpretation
- Range: 0 to 1
- Represents the fraction of answer claims supported by the context
- Score of 1.0 means all claims can be verified from the context
- Score of 0.0 means no claims can be verified from the context

```java
FaithfulnessEvaluator evaluator = new FaithfulnessEvaluator(chatModel);
Evaluation result = evaluator.evaluate(sample);
double faithfulnessScore = result.getValue();
```

## With images in the context

If the sample carries images (`sample.hasImages() == true`), construct the
evaluator with a vision-capable `ChatModel` and the explicit
`supportsVision=true` flag. The bot then receives both text context and
images when checking each claim. See [Image support](../image_support) for
details.

```java
FaithfulnessEvaluator visionEvaluator =
    new FaithfulnessEvaluator(gpt4oModel, /*supportsVision=*/ true);
```