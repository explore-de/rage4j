# BLEU Score

**Evaluator**: `BleuScoreEvaluator`

The BLEU score evaluates the similarity between an LLM-generated answer and a ground truth reference by computing the
**weighted geometric mean of clipped n-gram precisions** (from unigrams up to 4-grams). It includes a **brevity penalty**
to penalize overly short answers that might otherwise score high due to precision alone.

**Modified precision** measures how many of the answer’s n-grams appear in the ground truth but limits repeated matches
to avoid overcounting.

## Required Sample Fields

- `answer` – Required for n-gram overlap comparison.
- `groundTruth` – Required for n-gram overlap comparison.

## How It Works

1. Splits both the answer and the ground truth into tokens based on whitespace.
2. Calculates modified n-gram overlap precision (with clipping) between the answer and the ground truth for n = 1 to 4.
3. Computes the geometric mean of these n-gram precisions, with smoothing.
4. Applies a **brevity penalty** to reduce the score if the answer is significantly shorter than the ground truth.

## Score Interpretation

- **Range**: `0.0` (no overlap) to `1.0` (perfect match)
- **Higher scores** indicate closer word-level similarity between the answer and the reference, with better preservation
  of multi-word phrases.
- **Lower scores** may indicate missing content, incorrect phrasing, or overly short answers.
- BLEU is best suited for comparing structured or templated outputs where exact phrasing matters, but it may not capture
  semantic similarity well in more flexible, creative text.

## Example Usage

```java
BleuScoreEvaluator evaluator = new BleuScoreEvaluator();
Evaluation result = evaluator.evaluate(sample);
double bleuScore = result.getValue();
```
