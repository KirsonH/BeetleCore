package com.beetlecore.engine.infrastructure;

import io.helidon.config.Config;
import io.helidon.config.ConfigSources;

public final class ApplicationConfig {

    private static final Config CONFIG = Config.builder()
            .sources(ConfigSources.classpath("application.yaml"), ConfigSources.environmentVariables(), ConfigSources.systemProperties())
            .build();

    private ApplicationConfig() {
    }

    public static Config config() {
        return CONFIG;
    }

    public static String getString(String key, String defaultValue) {
        return CONFIG.get(key).asString().orElseGet(() -> {
            String env = System.getenv(toEnvVar(key));
            return env != null && !env.isBlank() ? env : defaultValue;
        });
    }

    public static int getInt(String key, int defaultValue) {
        return CONFIG.get(key).asInt().orElseGet(() -> {
            String env = System.getenv(toEnvVar(key));
            if (env != null && !env.isBlank()) {
                try {
                    return Integer.parseInt(env);
                } catch (NumberFormatException ignored) {
                }
            }
            return defaultValue;
        });
    }

    private static String toEnvVar(String key) {
        return key.toUpperCase().replace('.', '_');
    }
}
