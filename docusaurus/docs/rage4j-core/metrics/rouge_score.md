# ROUGE Score

**Evaluator**: `RougeScoreEvaluator`

The ROUGE score evaluates the content overlap between an LLM-generated answer and a ground truth reference. It is
commonly used for tasks like summarization and text generation. This evaluator supports multiple ROUGE variants,
including unigram/bigram overlap and sequence-based matching, as well as different scoring methods (Precision, Recall,
F1 score).

By default, it uses **ROUGE-1 F1 score**, which balances unigram precision and recall.

---

## Supported ROUGE Types

- `ROUGE-1` – Unigram overlap (individual words)
- `ROUGE-` – Bigram overlap (pairs of words)
- `ROUGE-L` – Longest common subsequence (LCS-based)
- `ROUGE-LSum` – LCS-based score optimized for multi-sentence summaries (used in summarization tasks)

---

## Supported Measure Types

- `PRECISION` – Proportion of matched units in the answer
- `RECALL` – Proportion of matched units in the reference
- `F1SCORE` – Harmonic mean of precision and recall

---

## Required Sample Fields

- `answer` – Required for n-gram or sequence overlap comparison.
- `groundTruth` – Required for n-gram or sequence overlap comparison.

---

## How It Works

1. Splits both the answer and the ground truth into tokens based on whitespace.
2. Based on the selected `RougeType`:
    - `ROUGE-1` and `ROUGE-2` compare n-gram overlap.
    - `ROUGE-L` computes the longest common subsequence (LCS).
    - `ROUGE-LSum` groups the tokens into sentences using '\n' as sentence boundary and applies LCS across all answer
      and ground truth sentence pairs for more accurate summary evaluation.
3. Calculates:
    - **Precision**: proportion of overlapping units in the answer.
    - **Recall**: proportion of overlapping units in the reference.
    - **F1 Score**: harmonic mean of precision and recall.
4. Returns the selected metric as the final score.

---

## Score Interpretation

## Score Interpretation

- **Range**: `0.0` (no overlap) to `1.0` (perfect match)
- **Higher scores** indicate stronger content overlap between the LLM-generated answer and the reference.

- **ROUGE-1** – A high score indicates strong alignment with the important words used in the reference.

- **ROUGE-2** – A high score suggests the answer closely follows the phrasing and local word order of the reference.

- **ROUGE-L** – A high score reflects that the answer preserves the overall structure and ordering of content from the
  reference.

- **ROUGE-LSum** – A high score means the answer captures the main ideas and preserves the overall flow of information
  across sentences, making it especially useful for evaluating summaries.

---

## Example Usage

```java
// Default: ROUGE-1 F1 score
RougeScoreEvaluator evaluator = new RougeScoreEvaluator();

// Custom: ROUGE-2 Precision
RougeScoreEvaluator customEvaluator = new RougeScoreEvaluator(RougeType.ROUGE2, MeasureType.PRECISION);

// Custom: ROUGE-LSum Recall
RougeScoreEvaluator summaryEvaluator = new RougeScoreEvaluator(RougeType.ROUGELSUM, MeasureType.RECALL);

Evaluation result = evaluator.evaluate(sample);
double rougeScore = result.getValue();
```
