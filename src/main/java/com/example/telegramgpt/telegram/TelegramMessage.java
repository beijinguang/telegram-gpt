package com.example.telegramgpt.telegram;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TelegramMessage(
        @JsonProperty("message_id") long messageId,
        TelegramChat chat,
        String text
) {
}
