package com.example.telegramgpt.telegram;

import com.example.telegramgpt.config.TelegramProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TelegramPollingService implements SmartLifecycle {
    private static final Logger log = LoggerFactory.getLogger(TelegramPollingService.class);

    private final TelegramClient telegramClient;
    private final TelegramMessageHandler messageHandler;
    private final TelegramProperties properties;

    private volatile boolean running;
    private volatile Thread pollingThread;
    private long nextOffset;

    public TelegramPollingService(
            TelegramClient telegramClient,
            TelegramMessageHandler messageHandler,
            TelegramProperties properties
    ) {
        this.telegramClient = telegramClient;
        this.messageHandler = messageHandler;
        this.properties = properties;
    }

    @Override
    public synchronized void start() {
        if (running || !properties.polling().enabled()) {
            return;
        }

        running = true;
        pollingThread = Thread.ofVirtual()
                .name("telegram-long-polling")
                .start(this::pollLoop);
        log.info("Telegram long polling started");
    }

    private void pollLoop() {
        while (running) {
            try {
                List<TelegramUpdate> updates = telegramClient.getUpdates(
                        nextOffset,
                        properties.polling().timeoutSeconds());
                for (TelegramUpdate update : updates) {
                    nextOffset = Math.max(nextOffset, update.updateId() + 1);
                    messageHandler.handle(update);
                }
            } catch (Exception exception) {
                if (running) {
                    log.warn("Telegram polling failed; retrying shortly: {}", exception.getMessage());
                    sleepBeforeRetry();
                }
            }
        }
    }

    private void sleepBeforeRetry() {
        try {
            Thread.sleep(2_000);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public synchronized void stop() {
        running = false;
        Thread thread = pollingThread;
        if (thread != null) {
            thread.interrupt();
            pollingThread = null;
        }
        log.info("Telegram long polling stopped");
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE;
    }
}
