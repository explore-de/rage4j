---
title: Answer Correctness
sidebar_position: 3
---

# Answer Correctness

**Evaluator**: `AnswerCorrectnessEvaluator`

Assesses the factual accuracy of the answer by comparing it with the ground truth using claim-based analysis.

## Required Sample fields
- `answer` - Required for claim extraction
- `groundTruth` - Required for comparison with answer

## How it works
1. Extracts claims from both the answer and ground truth
2. Identifies:
   - True positive claims (present in both answer and ground truth)
   - False positive claims (present in answer but not in ground truth)
   - False negative claims (present in ground truth but not in answer)
3. Calculates an F1 score based on these metrics

## Score interpretation
- Range: 0 to 1
- Score is based on F1 metric balancing precision and recall
- Higher scores indicate better alignment with the ground truth
- Perfect score (1.0) means all claims match the ground truth exactly

```java
AnswerCorrectnessEvaluator evaluator = new AnswerCorrectnessEvaluator(chatModel);
Evaluation result = evaluator.evaluate(sample);
double correctnessScore = result.getValue();
```