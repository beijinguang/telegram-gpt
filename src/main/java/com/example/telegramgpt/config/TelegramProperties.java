package com.example.telegramgpt.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "telegram")
public record TelegramProperties(
        @NotBlank String botToken,
        String apiBaseUrl,
        Polling polling
) {
    public TelegramProperties {
        apiBaseUrl = apiBaseUrl == null || apiBaseUrl.isBlank()
                ? "https://api.telegram.org"
                : apiBaseUrl;
        polling = polling == null ? new Polling(true, 30) : polling;
    }

    public record Polling(boolean enabled, int timeoutSeconds) {
        public Polling {
            if (timeoutSeconds < 1 || timeoutSeconds > 50) {
                timeoutSeconds = 30;
            }
        }
    }
}
