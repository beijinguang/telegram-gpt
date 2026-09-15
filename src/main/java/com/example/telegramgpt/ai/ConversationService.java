package com.example.telegramgpt.ai;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class ConversationService {
    private final OpenAiClient openAiClient;
    private final ConcurrentMap<Long, String> lastResponseIds = new ConcurrentHashMap<>();

    public ConversationService(OpenAiClient openAiClient) {
        this.openAiClient = openAiClient;
    }

    public String reply(long chatId, String userInput) {
        String previousResponseId = lastResponseIds.get(chatId);
        OpenAiResponse response = openAiClient.createResponse(userInput, previousResponseId);
        lastResponseIds.put(chatId, response.id());
        return response.outputText();
    }

    public void reset(long chatId) {
        lastResponseIds.remove(chatId);
    }

    String lastResponseIdForTest(long chatId) {
        return lastResponseIds.get(chatId);
    }
}
