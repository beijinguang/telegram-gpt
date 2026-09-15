package com.example.telegramgpt.telegram;

import com.example.telegramgpt.ai.ConversationService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class TelegramMessageHandlerTest {
    private final TelegramClient telegramClient = mock(TelegramClient.class);
    private final ConversationService conversationService = mock(ConversationService.class);
    private final TelegramMessageHandler handler = new TelegramMessageHandler(telegramClient, conversationService);

    @Test
    void sendsConversationReplyToTheChat() {
        when(conversationService.reply(42L, "你好")).thenReturn("你好，我是 GPT。");

        handler.handle(update(42L, "你好"));

        verify(telegramClient).sendMessage(42L, "你好，我是 GPT。");
        verify(conversationService).reply(42L, "你好");
    }

    @Test
    void newCommandResetsOnlyTheCurrentChat() {
        handler.handle(update(42L, "/new"));

        verify(conversationService).reset(42L);
        verify(telegramClient).sendMessage(42L, "已重置当前会话。我们重新开始吧。");
    }

    @Test
    void ignoresNonTextUpdates() {
        handler.handle(new TelegramUpdate(1L, new TelegramMessage(2L, new TelegramChat(42L), null)));

        verifyNoInteractions(telegramClient, conversationService);
    }

    @Test
    void splitsLongRepliesForTelegram() {
        List<String> parts = TelegramMessageHandler.splitMessage("a".repeat(8193));

        assertThat(parts).hasSize(3);
        assertThat(parts.get(0)).hasSize(4096);
        assertThat(parts.get(1)).hasSize(4096);
        assertThat(parts.get(2)).hasSize(1);
    }

    private static TelegramUpdate update(long chatId, String text) {
        return new TelegramUpdate(1L, new TelegramMessage(2L, new TelegramChat(chatId), text));
    }
}
