package com.example.telegramgpt.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OpenAiClientTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void extractsTextFromResponsesApiOutputItems() throws Exception {
        JsonNode response = objectMapper.readTree("""
                {
                  "output": [
                    {"type":"reasoning", "content":[]},
                    {"type":"message", "content":[
                      {"type":"output_text", "text":"第一段"},
                      {"type":"output_text", "text":"第二段"}
                    ]}
                  ]
                }
                """);

        assertThat(OpenAiClient.extractOutputText(response)).isEqualTo("第一段\n第二段");
    }

    @Test
    void supportsConvenienceTopLevelOutputText() throws Exception {
        JsonNode response = objectMapper.readTree("{\"output_text\":\"直接返回\"}");

        assertThat(OpenAiClient.extractOutputText(response)).isEqualTo("直接返回");
    }
}
