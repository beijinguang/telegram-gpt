package com.example.telegramgpt.ai;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConversationServiceTest {
    private final OpenAiClient openAiClient = mock(OpenAiClient.class);
    private final ConversationService conversationService = new ConversationService(openAiClient);

    @Test
    void keepsThePreviousResponseIdForTheSameChat() {
        when(openAiClient.createResponse("第一句", null))
                .thenReturn(new OpenAiResponse("resp-1", "第一句回复"));
        when(openAiClient.createResponse("第二句", "resp-1"))
                .thenReturn(new OpenAiResponse("resp-2", "第二句回复"));

        assertThat(conversationService.reply(100L, "第一句")).isEqualTo("第一句回复");
        assertThat(conversationService.reply(100L, "第二句")).isEqualTo("第二句回复");

        verify(openAiClient).createResponse("第一句", null);
        verify(openAiClient).createResponse("第二句", "resp-1");
        assertThat(conversationService.lastResponseIdForTest(100L)).isEqualTo("resp-2");
    }

    @Test
    void resetStartsTheNextMessageWithoutContext() {
        when(openAiClient.createResponse("之前", null))
                .thenReturn(new OpenAiResponse("resp-old", "旧回复"));
        when(openAiClient.createResponse("之后", null))
                .thenReturn(new OpenAiResponse("resp-new", "新回复"));

        conversationService.reply(200L, "之前");
        conversationService.reset(200L);
        conversationService.reply(200L, "之后");

        verify(openAiClient).createResponse("之后", null);
        assertThat(conversationService.lastResponseIdForTest(200L)).isEqualTo("resp-new");
    }
}
