package dev.rage4j.examples;

import dev.langchain4j.model.ollama.OllamaChatModel;

public class SimpleChatbot {

    private final OllamaChatModel chatModel;

    public SimpleChatbot(String baseUrl, String modelName) {
        this.chatModel = OllamaChatModel.builder()
            .baseUrl(baseUrl)
            .modelName(modelName)
            .temperature(0.0)
            .build();
    }

    public String askQuestion(String question) {
        return chatModel.chat(question);
    }
}

