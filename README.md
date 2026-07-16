[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=explore-de_rage4j&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=explore-de_rage4j)
[![Docusaurus Build And Deploy](https://github.com/explore-de/rage4j/actions/workflows/docusaurus.yml/badge.svg)](https://github.com/explore-de/rage4j/actions/workflows/docusaurus.yml)

<div align="center">
    <img src="docusaurus/static/img/rage4j.png" alt="rage4j" width="450" height="450">
</div>

# Rage4J

RAG Evaluation library for Java.

## Overview

Rage4J provides tools to evaluate and measure the quality of language model outputs using various metrics like correctness, relevance, faithfulness, and semantic similarity. It integrates with LangChain4j and supports fluent test assertions for RAG pipelines.

**Modules:**
- **rage4j** - Core evaluation library with evaluators and model classes
- **rage4j-assert** - Fluent assertion library for RAG evaluation in tests
- **rage4j-persist** - Persistence module for saving evaluation results (JSONL and HTML reports)
- **rage4j-persist-junit5** - JUnit 5 extension for evaluation persistence and HTML artifacts

## Installation

Add the dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>dev.rage4j</groupId>
    <artifactId>rage4j</artifactId>
    <version>2.0.1-SNAPSHOT</version>
</dependency>
```

For fluent test assertions:

```xml
<dependency>
    <groupId>dev.rage4j</groupId>
    <artifactId>rage4j-assert</artifactId>
    <version>2.0.1-SNAPSHOT</version>
    <scope>test</scope>
</dependency>
```

For persistence of evaluations:

```xml
<dependency>
    <groupId>dev.rage4j</groupId>
    <artifactId>rage4j-persist</artifactId>
    <version>2.0.1-SNAPSHOT</version>
    <scope>test</scope>
</dependency>
```

For JUnit 5 persistence and HTML reports:

```xml
<dependency>
    <groupId>dev.rage4j</groupId>
    <artifactId>rage4j-persist-junit5</artifactId>
    <version>2.0.1-SNAPSHOT</version>
    <scope>test</scope>
</dependency>
```

## Usage

### Core Evaluation

```java
Sample sample = Sample.builder()
    .withQuestion("What is the capital of France?")
    .withAnswer("Paris is the capital of France.")
    .withGroundTruth("Paris")
    .build();

Evaluator evaluator = new AnswerCorrectnessEvaluator(chatModel);
Evaluation result = evaluator.evaluate(sample);
System.out.println(result.getName() + ": " + result.getValue());
```

### Fluent Assertions

```java
RageAssert rageAssert = new OpenAiLLMBuilder().fromApiKey(apiKey);

rageAssert.given()
    .question("What is the capital of France?")
    .groundTruth("Paris")
    .context("Paris is the capital of France.")
    .when()
    .answer("Paris is the capital of France.")
    .then()
    .assertFaithfulness(0.7)
    .then()
    .assertAnswerCorrectness(0.8);
```

### HTML evaluation report for Jenkins

Annotate an AI integration-test class with `@Rage4jHtmlReport`. Rage4j injects an `EvaluationStore` and writes a self-contained HTML artifact after the class completes.

```java
import org.junit.jupiter.api.Test;

import dev.rage4j.persist.EvaluationStore;
import dev.rage4j.persist.junit5.Rage4jHtmlReport;

@Rage4jHtmlReport(file = "target/rage4j-report.html")
class AiEvaluationTest
{
    @Test
    void evaluatesAnswer(EvaluationStore store)
    {
        EvaluationAggregation result = rageAssert.given()
            .question("What is the capital of France?")
            .groundTruth("Paris")
            .context("Paris is the capital of France.")
            .when()
            .answer("Paris")
            .then()
            .assertFaithfulness(0.7)
            .getEvaluationAggregation();

        store.store(result);
    }
}
```

Archive `**/target/rage4j-report.html` in Jenkins, or publish it with the HTML Publisher plugin.

<details>
<summary>Generated HTML example</summary>

```html
<!doctype html><html lang="en"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1"><title>Rage4j evaluation report</title><style>body{font-family:system-ui,sans-serif;max-width:1200px;margin:2rem auto;padding:0 1rem;color:#172033}table{border-collapse:collapse;width:100%;margin:1rem 0}th,td{border:1px solid #d7dce5;padding:.65rem;text-align:left;vertical-align:top}th{background:#f3f5f8}details{max-width:36rem;white-space:pre-wrap}summary{cursor:pointer} .score{font-variant-numeric:tabular-nums}</style></head><body><h1>Rage4j evaluation report</h1><p>Generated 2026-07-16T11:48:38.317199+02:00 · 1 evaluation(s)</p><table><thead><tr><th>#</th><th>Question</th><th>Metrics</th><th>Answer</th></tr></thead><tbody><tr><td>1</td><td>What is the capital of France?</td><td><ul><li><strong>AnswerCorrectness</strong>: <span class="score">1.000</span></li><li><strong>Faithfulness</strong>: <span class="score">1.000</span></li></ul></td><td>Paris is the capital of France.</td></tr></tbody></table></body></html>
```

</details>

## Documentation
Visit our documentation on Github Pages: <a href="https://explore-de.github.io/rage4j/" target="_blank">Visit Docs</a>

## Requirements

- Java 21
- Maven (wrapper included: `./mvnw`)

For development, use the code formatter (`./mvnw formatter:format`) and install the EditorConfig extension (IntelliJ has built-in support).


## Contributors

<!-- ALL-CONTRIBUTORS-LIST:START - Do not remove or modify this section -->
<!-- prettier-ignore-start -->
<!-- markdownlint-disable -->
<table>
  <tbody>
    <tr>
      <td align="center" valign="top" width="14.28%"><a href="http://ris5266.github.io"><img src="https://avatars.githubusercontent.com/u/86254687?v=4?s=100" width="100px;" alt="richard"/><br /><sub><b>richard</b></sub></a><br /><a href="#code-ris5266" title="Code">💻</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://social.mymiggi.de/@miggi"><img src="https://avatars.githubusercontent.com/u/70092362?v=4?s=100" width="100px;" alt="Michael Hainz"/><br /><sub><b>Michael Hainz</b></sub></a><br /><a href="#code-MiggiV2" title="Code">💻</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/vladislavkn"><img src="https://avatars.githubusercontent.com/u/51641565?v=4?s=100" width="100px;" alt="Vladislav Knyshov"/><br /><sub><b>Vladislav Knyshov</b></sub></a><br /><a href="#code-vladislavkn" title="Code">💻</a></td>
      <td align="center" valign="top" width="14.28%"><a href="http://explore.de"><img src="https://avatars.githubusercontent.com/u/545499?v=4?s=100" width="100px;" alt="Markus Herhoffer"/><br /><sub><b>Markus Herhoffer</b></sub></a><br /><a href="#projectManagement-d135-1r43" title="Project Management">📆</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/babyygemperor"><img src="https://avatars.githubusercontent.com/u/25747019?v=4?s=100" width="100px;" alt="Aamin Gem"/><br /><sub><b>Aamin Gem</b></sub></a><br /><a href="#code-babyygemperor" title="Code">💻</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/vvilip"><img src="https://avatars.githubusercontent.com/u/115623345?v=4?s=100" width="100px;" alt="DrBilip"/><br /><sub><b>DrBilip</b></sub></a><br /><a href="#code-vvilip" title="Code">💻</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/Andy1734"><img src="https://avatars.githubusercontent.com/u/94300201?v=4?s=100" width="100px;" alt="Andreas Dinauer"/><br /><sub><b>Andreas Dinauer</b></sub></a><br /><a href="#code-Andy1734" title="Code">💻</a></td>
    </tr>
    <tr>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/kmscheuer"><img src="https://avatars.githubusercontent.com/u/3342781?v=4?s=100" width="100px;" alt="Klaus-Martin Fink"/><br /><sub><b>Klaus-Martin Fink</b></sub></a><br /><a href="#code-kmscheuer" title="Code">💻</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/andresCh01"><img src="https://avatars.githubusercontent.com/u/196327563?v=4?s=100" width="100px;" alt="andresCh01"/><br /><sub><b>andresCh01</b></sub></a><br /><a href="#code-andresCh01" title="Code">💻</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/Ainges"><img src="https://avatars.githubusercontent.com/u/81434615?v=4?s=100" width="100px;" alt="Hubertus Seitz"/><br /><sub><b>Hubertus Seitz</b></sub></a><br /><a href="#code-Ainges" title="Code">💻</a></td>
    </tr>
  </tbody>
</table>

<!-- markdownlint-restore -->
<!-- prettier-ignore-end -->

<!-- ALL-CONTRIBUTORS-LIST:END -->
