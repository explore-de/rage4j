package dev.rage4j.examples;

import io.github.cdimascio.dotenv.Dotenv;

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
}

