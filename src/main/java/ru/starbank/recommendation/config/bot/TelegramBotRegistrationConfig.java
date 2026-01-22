package ru.starbank.recommendation.config.bot;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

/**
 * Явная регистрация long-polling бота.
 * <p>Отключается в тестах через telegram.bot.enabled=false</p>
 *
 * <p>Дополнительно содержит "внятную" диагностику наиболее частых проблем:</p>
 * <ul>
 *   <li>401 Unauthorized — неверный токен или токен не подставился из env</li>
 *   <li>409 Conflict — бот уже запущен где-то ещё (другой процесс getUpdates)</li>
 * </ul>
 */
@Configuration
@ConditionalOnProperty(prefix = "telegram.bot", name = "enabled", havingValue = "true", matchIfMissing = true)
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
        } catch (TelegramApiRequestException e) {
            int code = e.getErrorCode();
            String apiResponse = e.getApiResponse();

            if (code == 401) {
                log.error(
                        "Telegram bot registration failed: 401 Unauthorized. " +
                                "Проверь TELEGRAM_BOT_TOKEN (env/Run Configuration). username={}, apiResponse={}",
                        safeUsername(), apiResponse
                );
            } else if (code == 409) {
                log.error(
                        "Telegram bot registration failed: 409 Conflict. " +
                                "Скорее всего бот уже запущен в другом процессе (getUpdates conflict). " +
                                "Останови другой экземпляр и перезапусти. username={}, apiResponse={}",
                        safeUsername(), apiResponse
                );
            } else {
                log.error("Telegram bot registration failed: errorCode={}, username={}, apiResponse={}",
                        code, safeUsername(), apiResponse, e);
            }

            throw e;
        } catch (TelegramApiException e) {
            // На случай других ошибок библиотеки
            log.error("Telegram bot registration failed (TelegramApiException). username={}", safeUsername(), e);
            throw e;
        }
    }

    private String safeUsername() {
        try {
            return telegramBot.getBotUsername();
        } catch (Exception ex) {
            return "<unknown>";
        }
    }
}