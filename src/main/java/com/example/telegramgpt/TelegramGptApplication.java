package com.example.telegramgpt;

import com.example.telegramgpt.config.OpenAiProperties;
import com.example.telegramgpt.config.TelegramProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({TelegramProperties.class, OpenAiProperties.class})
public class TelegramGptApplication {

    public static void main(String[] args) {
        SpringApplication.run(TelegramGptApplication.class, args);
    }
}
