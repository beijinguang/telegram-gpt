package com.example.telegramgpt.ai;

import com.example.telegramgpt.config.OpenAiProperties;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class OpenAiClient {
    private final RestClient restClient;
    private final OpenAiProperties properties;

    public OpenAiClient(RestClient.Builder restClientBuilder, OpenAiProperties properties) {
        this.restClient = restClientBuilder
                .baseUrl(properties.baseUrl())
                .defaultHeader("Authorization", "Bearer " + properties.apiKey())
                .build();
        this.properties = properties;
    }

    public OpenAiResponse createResponse(String userInput, String previousResponseId) {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("model", properties.model());
        request.put("input", userInput);
        request.put("store", true);
        if (previousResponseId != null && !previousResponseId.isBlank()) {
            request.put("previous_response_id", previousResponseId);
        }

        JsonNode response = restClient.post()
                .uri("/responses")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(JsonNode.class);

        if (response == null) {
            throw new IllegalStateException("OpenAI returned an empty response");
        }

        String id = response.path("id").asText(null);
        String outputText = extractOutputText(response);
        if (id == null || id.isBlank()) {
            throw new IllegalStateException("OpenAI response did not contain an id");
        }
        if (outputText.isBlank()) {
            throw new IllegalStateException("OpenAI response did not contain output text");
        }
        return new OpenAiResponse(id, outputText);
    }

    static String extractOutputText(JsonNode response) {
        StringBuilder text = new StringBuilder();

        JsonNode topLevelText = response.get("output_text");
        if (topLevelText != null && topLevelText.isTextual()) {
            return topLevelText.asText();
        }

        JsonNode output = response.path("output");
        if (output.isArray()) {
            for (JsonNode outputItem : output) {
                JsonNode content = outputItem.path("content");
                if (!content.isArray()) {
                    continue;
                }
                for (JsonNode contentItem : content) {
                    if ("output_text".equals(contentItem.path("type").asText())
                            && contentItem.path("text").isTextual()) {
                        if (text.length() > 0) {
                            text.append('\n');
                        }
                        text.append(contentItem.path("text").asText());
                    }
                }
            }
        }
        return text.toString();
    }
}
