package dev.rage4j.config;

import java.util.Properties;
import java.util.Set;

public class ConfigFactory {
    private static final AppConfig INSTANCE;
    private static final Set<String> ENV_ONLY_KEYS = Set.of(
            "open.ai.key"
    );


    static {
        Properties props = new Properties();
        try (var is = ConfigFactory.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (is != null) {
                props.load(is);
                validateNoSecrets(props);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error in ConfigFactory while loading config: " + e.getMessage(), e);
        }

        // Load config values, preferring env vars over properties, and applying defaults if neither is set
        INSTANCE = new AppConfig(
                envOrProp("OPEN_AI_MODEL", props, "gpt-5.2-2025-12-11"),
                envOrProp("OPEN_AI_KEY", props, ""),
                envOrProp("OPEN_AI_EMBEDDING_MODEL", props, "text-embedding-3-large")
        );
    }

    private static String envOrProp(String envKey, Properties props, String defaultVal) {
        String envVal = System.getenv(envKey);
        if (envVal != null) return envVal;
        String propVal = props.getProperty(envKey.replace("_", ".").toLowerCase());
        return propVal != null ? propVal : defaultVal;
    }

    private static void validateNoSecrets(Properties props) {
        ENV_ONLY_KEYS.forEach(key -> {
            if (props.containsKey(key)) {
                throw new IllegalStateException(
                        "WARNING: Secret-Key '" + key +
                        "' in application.properties found! Only allowed as env key: " +
                        key.toUpperCase().replace(".", "_")
                );
            }
        });
    }

    public static AppConfig getConfig() { return INSTANCE; }
}
