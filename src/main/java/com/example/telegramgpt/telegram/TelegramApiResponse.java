package com.example.telegramgpt.telegram;

public record TelegramApiResponse<T>(boolean ok, T result, String description) {
}
