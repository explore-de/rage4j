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
        String response = chatModel.chat(question);
        return stripThinking(response);
    }

    public String askQuestionWithContext(String question, String context) {
        String prompt = String.format(
            "Context: %s\n\nQuestion: %s\n\nAnswer based on the context provided:",
            context, question
        );
        String response = chatModel.chat(prompt);
        return stripThinking(response);
    }

    private String stripThinking(String response) {
        if (response.contains("</think>")) {
            return response.substring(response.lastIndexOf("</think>") + 8).trim();
        }
        return response;
    }
}

