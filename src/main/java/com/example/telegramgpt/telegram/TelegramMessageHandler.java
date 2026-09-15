package com.example.telegramgpt.telegram;

import com.example.telegramgpt.ai.ConversationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component
public class TelegramMessageHandler {
    private static final Logger log = LoggerFactory.getLogger(TelegramMessageHandler.class);
    private static final int TELEGRAM_TEXT_LIMIT = 4096;

    private final TelegramClient telegramClient;
    private final ConversationService conversationService;

    public TelegramMessageHandler(TelegramClient telegramClient, ConversationService conversationService) {
        this.telegramClient = telegramClient;
        this.conversationService = conversationService;
    }

    public void handle(TelegramUpdate update) {
        if (update == null || update.message() == null || update.message().chat() == null) {
            return;
        }

        TelegramMessage message = update.message();
        if (message.text() == null || message.text().isBlank()) {
            return;
        }

        long chatId = message.chat().id();
        String text = message.text().trim();

        try {
            if (isCommand(text, "/new")) {
                conversationService.reset(chatId);
                telegramClient.sendMessage(chatId, "已重置当前会话。我们重新开始吧。");
                return;
            }

            if (isCommand(text, "/start") || isCommand(text, "/help")) {
                telegramClient.sendMessage(chatId,
                        "你好！我是 Telegram GPT。直接发送文字即可开始对话。\n\n"
                                + "发送 /new 可以清空当前会话并重新开始。");
                return;
            }

            String answer = conversationService.reply(chatId, text);
            for (String part : splitMessage(answer)) {
                telegramClient.sendMessage(chatId, part);
            }
        } catch (Exception exception) {
            log.error("Failed to process Telegram message from chat {}", chatId, exception);
            telegramClient.sendMessage(chatId, "抱歉，刚才处理消息时出了点问题，请稍后再试。");
        }
    }

    private static boolean isCommand(String text, String command) {
        String firstToken = text.split("\\s+", 2)[0].toLowerCase(Locale.ROOT);
        String normalizedCommand = command.toLowerCase(Locale.ROOT);
        return firstToken.equals(normalizedCommand) || firstToken.startsWith(normalizedCommand + "@");
    }

    static List<String> splitMessage(String text) {
        if (text == null || text.isBlank()) {
            return List.of("（模型没有返回文字内容。）");
        }
        if (text.length() <= TELEGRAM_TEXT_LIMIT) {
            return List.of(text);
        }

        java.util.ArrayList<String> parts = new java.util.ArrayList<>();
        for (int start = 0; start < text.length(); start += TELEGRAM_TEXT_LIMIT) {
            parts.add(text.substring(start, Math.min(start + TELEGRAM_TEXT_LIMIT, text.length())));
        }
        return parts;
    }
}
