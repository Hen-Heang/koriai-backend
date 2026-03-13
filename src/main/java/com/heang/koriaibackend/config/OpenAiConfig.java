package com.heang.koriaibackend.config;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

@Configuration
public class OpenAiConfig {

    private static final Logger log = LoggerFactory.getLogger(OpenAiConfig.class);

    @Value("${openai.mock-enabled:false}")
    private boolean mockEnabled;

    @Value("${openai.api-key:}")
    private String configuredApiKey;

    @Bean
    OpenAIClient openAIClient() {
        if (mockEnabled) {
            log.info("OpenAI client initialized in mock mode (openai.mock-enabled=true)");
            return OpenAIOkHttpClient.builder()
                    .apiKey("test-openai-key")
                    .build();
        }
        String envApiKey = System.getenv("OPENAI_API_KEY");
        String key = Optional.ofNullable(envApiKey)
                .filter(value -> !value.isBlank())
                .orElse(configuredApiKey);

        boolean hasEnvKey = envApiKey != null && !envApiKey.isBlank();
        boolean hasYamlKey = configuredApiKey != null && !configuredApiKey.isBlank();
        if (hasEnvKey) {
            log.info("OpenAI key source detected: OPENAI_API_KEY environment variable");
        } else if (hasYamlKey) {
            log.info("OpenAI key source detected: openai.api-key property");
        } else {
            log.error("OpenAI key not detected. Expected OPENAI_API_KEY or openai.api-key.");
        }

        if (key == null || key.isBlank()) {
            throw new IllegalStateException("OpenAI API key is missing. Set OPENAI_API_KEY or openai.api-key.");
        }

        return OpenAIOkHttpClient.builder()
                .apiKey(key)
                .build();
    }
}
