package com.example.telegramgpt.telegram;

import com.example.telegramgpt.config.TelegramProperties;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.List;

@Component
public class TelegramClient {
    private static final ParameterizedTypeReference<TelegramApiResponse<List<TelegramUpdate>>> UPDATES_TYPE =
            new ParameterizedTypeReference<>() {
            };

    private final RestClient restClient;
    private final TelegramProperties properties;

    public TelegramClient(RestClient.Builder restClientBuilder, TelegramProperties properties) {
        this.restClient = restClientBuilder
                .baseUrl(properties.apiBaseUrl())
                .build();
        this.properties = properties;
    }

    public List<TelegramUpdate> getUpdates(long offset, int timeoutSeconds) {
        TelegramApiResponse<List<TelegramUpdate>> response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(botPath("getUpdates"))
                        .queryParam("timeout", timeoutSeconds)
                        .queryParam("allowed_updates", "[\"message\"]")
                        .queryParam("offset", offset)
                        .build())
                .retrieve()
                .body(UPDATES_TYPE);

        ensureSuccess(response, "getUpdates");
        return response.result() == null ? Collections.emptyList() : response.result();
    }

    public void sendMessage(long chatId, String text) {
        TelegramApiResponse<Object> response = restClient.post()
                .uri(botPath("sendMessage"))
                .contentType(MediaType.APPLICATION_JSON)
                .body(new SendMessageRequest(chatId, text))
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });

        ensureSuccess(response, "sendMessage");
    }

    private String botPath(String method) {
        return "/bot" + properties.botToken() + "/" + method;
    }

    private static void ensureSuccess(TelegramApiResponse<?> response, String operation) {
        if (response == null) {
            throw new IllegalStateException("Telegram API returned an empty response during " + operation);
        }
        if (!response.ok()) {
            throw new IllegalStateException("Telegram API error during " + operation + ": "
                    + response.description());
        }
    }

    private record SendMessageRequest(
            @com.fasterxml.jackson.annotation.JsonProperty("chat_id") long chatId,
            String text
    ) {
    }
}
