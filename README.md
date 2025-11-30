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
- **rage4j-persist** - Persistence module for saving evaluation results (JSONL format)
- **rage4j-persist-junit5** - JUnit 5 extension for automatic persistence lifecycle

## Installation

Add the dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>dev.rage4j</groupId>
    <artifactId>rage4j</artifactId>
    <version>1.1.1-SNAPSHOT</version>
</dependency>
```

For fluent test assertions:

```xml
<dependency>
    <groupId>dev.rage4j</groupId>
    <artifactId>rage-assert</artifactId>
    <version>1.1.1-SNAPSHOT</version>
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
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/ris5266"><img src="https://avatars.githubusercontent.com/u/86254687?v=4?s=100" width="100px;" alt="richard"/><br /><sub><b>richard</b></sub></a><br /><a href="#code-ris5266" title="Code">💻</a></td>
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
    </tr>
  </tbody>
</table>

<!-- markdownlint-restore -->
<!-- prettier-ignore-end -->

<!-- ALL-CONTRIBUTORS-LIST:END -->
