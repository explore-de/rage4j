---
title: Image support
sidebar_position: 3
---

# Image support

RAGE4j evaluators can pass images to the judging LLM alongside the textual
context. This is intended for RAG systems where the answer was produced from a
mix of text and images (e.g. diagrams, charts, screenshots, photographs) and
the evaluator needs to "see" the same images to make a fair judgment.

## When to use

Images are part of the **context** &mdash; what the system under test had to work
with &mdash; not part of the question. Today they are forwarded by:

- `FaithfulnessEvaluator` &ndash; checks each answer claim against text context
  **plus** images.
- `ContextRelevanceLlmEvaluator` &ndash; scores how relevant the combined
  text-and-image context is to the question.

Other evaluators (`AnswerCorrectness`, `AnswerRelevance`, `BLEU`, `ROUGE`,
`SemanticSimilarity`) deliberately ignore images. Their metrics are either
purely textual (correctness vs. ground truth) or numeric (n-gram / embedding
based) and would not benefit from a visual signal.

## Attaching images to a Sample

`Rage4jImage` exposes three factory methods. The image name is required for
persistence and is auto-derived where possible.

```java
import dev.rage4j.model.Rage4jImage;
import java.nio.file.Path;

Rage4jImage fromFile  = Rage4jImage.fromPath(Path.of("eiffel-tower.jpg"));
Rage4jImage fromUrl   = Rage4jImage.fromUrl("https://example.com/paris-map.png");
Rage4jImage fromBytes = Rage4jImage.fromBytes(bytes, "image/png", "louvre.png");

Sample sample = Sample.builder()
    .withQuestion("What landmarks are mentioned in the document?")
    .withContext("Paris is the capital of France and home to many landmarks.")
    .withImages(List.of(fromFile, fromUrl, fromBytes))
    .withAnswer(answer)
    .build();
```

`fromPath` reads the file eagerly and derives the MIME type from the extension
(`.png`, `.jpg/.jpeg`, `.gif`, `.webp`, `.bmp`).

## Vision-capable models

The judging `ChatModel` must support multimodal input (e.g. `gpt-4o`,
`gpt-4o-mini`). LangChain4j 1.x does not expose a vision capability flag on
`ChatModel`, so the evaluator cannot detect this automatically. You opt in
explicitly:

```java
ChatModel visionModel = OpenAiChatModel.builder()
    .apiKey(apiKey)
    .modelName("gpt-4o-mini")
    .build();

FaithfulnessEvaluator evaluator = new FaithfulnessEvaluator(visionModel, true);
ContextRelevanceLlmEvaluator ctx = new ContextRelevanceLlmEvaluator(visionModel, true);
```

If a sample contains images but the evaluator was constructed **without** the
vision flag, an `UnsupportedOperationException` is thrown before any LLM call:

```text
Faithfulness evaluator received a Sample with 3 image(s) but was not
configured for vision. Pass a vision-capable ChatModel (e.g. gpt-4o)
and use the constructor variant that takes supportsVision=true.
```

The text-only constructors (`new FaithfulnessEvaluator(model)`) keep their
original behaviour and are still the right choice for samples without images.

## End-to-end example

```java
ChatModel visionModel = OpenAiChatModel.builder()
    .apiKey(apiKey)
    .modelName("gpt-4o-mini")
    .build();

Sample sample = Sample.builder()
    .withQuestion("What landmarks are mentioned in the document?")
    .withContext("Paris is the capital of France and home to many landmarks.")
    .withImages(List.of(
        Rage4jImage.fromPath(Path.of("eiffel-tower.jpg")),
        Rage4jImage.fromPath(Path.of("louvre.png")),
        Rage4jImage.fromPath(Path.of("notre-dame.jpg"))))
    .withAnswer(answer)
    .withGroundTruth("Eiffel Tower, Louvre, and Notre-Dame are among the famous landmarks of Paris.")
    .build();

FaithfulnessEvaluator faithfulness =
    new FaithfulnessEvaluator(visionModel, true);
ContextRelevanceLlmEvaluator relevance =
    new ContextRelevanceLlmEvaluator(visionModel, true);

Evaluation faithfulnessScore = faithfulness.evaluate(sample);
Evaluation contextScore      = relevance.evaluate(sample);
```

## Persistence

When samples are written through the persist module, only image **names** are
stored &ndash; the bytes never reach the JSONL file:

```json
{
  "sample": {
    "question": "What landmarks are mentioned in the document?",
    "context": "Paris is the capital of France and home to many landmarks.",
    "images": ["eiffel-tower.jpg", "louvre.png", "notre-dame.jpg"]
  },
  "metrics": { "Faithfulness": 0.83, "Context relevance LLM": 1.0 }
}
```

If you need to re-run evaluations from a stored record, keep the original
images on disk and re-attach them via `Rage4jImage.fromPath(...)` using the
name as a lookup key.