package com.example.telegramgpt.telegram;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TelegramUpdate(
        @JsonProperty("update_id") long updateId,
        TelegramMessage message
) {
}
