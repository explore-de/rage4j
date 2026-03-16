package dev.rage4j.config;

public record AppConfig(
        String OPEN_AI_MODEL,
        String OPEN_AI_KEY,
        String OPEN_AI_EMBEDDING_MODEL
)
{}
