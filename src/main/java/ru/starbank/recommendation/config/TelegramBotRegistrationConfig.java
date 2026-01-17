package ru.starbank.recommendation.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

/**
 * Явная регистрация long-polling бота.
 *
 * <p>Нужна, чтобы бот гарантированно начинал получать обновления (getUpdates) в Spring Boot 3
 * даже если стартер/автоконфигурация не подхватила регистрацию.</p>
 */
@Configuration
public class TelegramBotRegistrationConfig {

    private static final Logger log = LoggerFactory.getLogger(TelegramBotRegistrationConfig.class);

    private final TelegramLongPollingBot telegramBot;

    public TelegramBotRegistrationConfig(TelegramLongPollingBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    @PostConstruct
    public void register() throws TelegramApiException {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(telegramBot);
            log.info("Telegram bot registered for long polling. username={}", telegramBot.getBotUsername());
        } catch (TelegramApiException e) {
            log.error("Failed to register Telegram bot for long polling.", e);
            throw e;
        }
    }
}