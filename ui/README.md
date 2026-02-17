# Rage4J Demo Application

A beautiful interactive demo application for [Rage4J](https://github.com/explore-de/rage4j) - the RAG Evaluation library for Java.

## Features

- **Interactive WebUI** - Beautiful dark-themed interface for running evaluations
- **Multiple Metrics** - Evaluate using Answer Correctness, Answer Relevance, Faithfulness, and Context Relevance
- **Pre-built Examples** - Load example scenarios to quickly test the evaluation system
- **Real-time Results** - See evaluation scores with visual progress bars and execution times

## Prerequisites

- Java 21+
- Maven 3.8+
- OpenAI API Key

## Quick Start

1. **Set your OpenAI API key:**

   ```bash
   export OPENAI_API_KEY=your-api-key-here
   ```

2. **Run the application:**

   ```bash
   ./mvnw quarkus:dev
   ```

3. **Open your browser:**

   Navigate to [http://localhost:8080](http://localhost:8080)

## Configuration

Configure via environment variables or `application.properties`:

| Variable | Description | Default |
|----------|-------------|---------|
| `OPENAI_API_KEY` | Your OpenAI API key | Optional (required for LLM metrics) |
| `OPENAI_MODEL` | OpenAI model to use | `gpt-4o-mini` |

**Note:** BLEU and ROUGE metrics work without an API key! Only LLM-based metrics require OpenAI.

## Available Metrics

### Algorithmic Metrics (No API Key Required)

| Metric | Description |
|--------|-------------|
| **BLEU Score** | Bilingual Evaluation Understudy - measures n-gram overlap between answer and ground truth |
| **ROUGE-1** | Measures unigram (single word) overlap between answer and ground truth |
| **ROUGE-2** | Measures bigram (two consecutive words) overlap |
| **ROUGE-L** | Measures longest common subsequence, capturing sentence-level structure |

### LLM-Based Metrics (Requires OpenAI API Key)

| Metric | Description |
|--------|-------------|
| **Answer Correctness** | LLM-based evaluation of factual accuracy compared to ground truth |
| **Answer Relevance** | LLM-based measurement of how relevant the answer is to the question |
| **Faithfulness** | Detects hallucinations by checking if answer is faithful to context |
| **Semantic Similarity** | Embedding-based semantic similarity using vector comparison |

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/health` | Health check and configuration status |
| `GET` | `/api/metrics` | List available metrics |
| `GET` | `/api/examples` | Get example evaluation scenarios |
| `POST` | `/api/evaluate` | Run evaluation |

### Example API Request

```bash
curl -X POST http://localhost:8080/api/evaluate \
  -H "Content-Type: application/json" \
  -d '{
    "question": "What is the capital of France?",
    "answer": "The capital of France is Paris.",
    "groundTruth": "Paris",
    "contexts": ["Paris is the capital city of France."],
    "metrics": ["answer_correctness", "faithfulness"]
  }'
```

## Project Structure

```
rage4j-demo/
├── src/main/java/dev/rage4j/demo/
│   ├── model/           # Request/Response DTOs
│   ├── resource/        # REST API endpoints
│   └── service/         # Evaluation service
├── src/main/resources/
│   ├── META-INF/resources/   # Static web files
│   │   ├── css/style.css
│   │   ├── js/app.js
│   │   └── index.html
│   └── application.properties
└── pom.xml
```

## License

This demo uses Rage4J which is available under its respective license.
