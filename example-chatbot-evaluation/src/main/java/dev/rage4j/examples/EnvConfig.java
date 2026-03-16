package dev.rage4j.examples;

import io.github.cdimascio.dotenv.Dotenv;

import java.util.Arrays;
import java.util.List;

public class EnvConfig {

    private static final Dotenv dotenv;

    static {
        dotenv = Dotenv.configure()
            .ignoreIfMissing()
            .load();
    }

    public static String getOllamaBaseUrl() {
        return get("OLLAMA_BASE_URL", "http://localhost:11434");
    }

    public static String get(String key, String defaultValue) {
        String value = dotenv.get(key);
        return value != null ? value : defaultValue;
    }

    public static int getInt(String key, int defaultValue) {
        String value = dotenv.get(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return Integer.parseInt(value.trim());
    }

    public static List<String> getList(String key) {
        String value = dotenv.get(key);
        if (value == null || value.isBlank()) {
            return List.of();
        }

        return Arrays.stream(value.split(","))
            .map(String::trim)
            .filter(entry -> !entry.isEmpty())
            .toList();
    }
}
