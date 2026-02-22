package dev.rage4j.examples;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;

public class SimpleChatbot {

    private final OllamaChatModel chatModel;

    public SimpleChatbot(String baseUrl, String modelName) {
        this.chatModel = OllamaChatModel.builder()
            .baseUrl(baseUrl)
            .modelName(modelName)
            .temperature(0.0)
            .timeout(java.time.Duration.ofMinutes(5))
            .build();
    }

    public String askQuestion(String question) {
        return chatModel.chat(question);
    }

    /** Exposes the underlying model, e.g. for gender-aware COBS evaluation. */
    public ChatModel getChatModel() {
        return chatModel;
    }
}

