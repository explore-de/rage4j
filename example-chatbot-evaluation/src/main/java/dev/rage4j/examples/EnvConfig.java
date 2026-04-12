package dev.rage4j.examples;

import io.github.cdimascio.dotenv.Dotenv;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class EnvConfig {

    private static final Dotenv dotenv;

    static {
        dotenv = loadDotenv();
    }

    private static Dotenv loadDotenv() {
        for (String directory : List.of(".", "example-chatbot-evaluation")) {
            if (Files.isRegularFile(Path.of(directory, ".env"))) {
                return Dotenv.configure()
                    .directory(directory)
                    .ignoreIfMissing()
                    .load();
            }
        }

        return Dotenv.configure()
            .ignoreIfMissing()
            .load();
    }

    public static String getOllamaBaseUrl() {
        return get("OLLAMA_BASE_URL", "http://localhost:11434");
    }

    public static String getOpenAiApiKey() {
        return get("OPENAI_API_KEY", null);
    }

    public static String get(String key, String defaultValue) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            value = dotenv.get(key);
        }
        return value != null ? value : defaultValue;
    }

    public static int getInt(String key, int defaultValue) {
        String value = get(key, null);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return Integer.parseInt(value.trim());
    }

    public static List<String> getList(String key) {
        String value = get(key, null);
        if (value == null || value.isBlank()) {
            return List.of();
        }

        return Arrays.stream(value.split(","))
            .map(String::trim)
            .filter(entry -> !entry.isEmpty())
            .toList();
    }
}
