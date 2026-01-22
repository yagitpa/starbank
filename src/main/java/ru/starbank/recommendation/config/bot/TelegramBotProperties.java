package ru.starbank.recommendation.config.bot;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Настройки Telegram Bot из application.yml.
 * В файле указаны значения по умолчанию.
 * application.yml -> если значения есть, применяются. Если нет -> TelegramBotProperties
 *
 * <p>Токен и username могут подтягиваться из env через ${...}.</p>
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "telegram.bot")
public class TelegramBotProperties {

    private String token;
    private String username;

    private final RateLimit rateLimit = new RateLimit();

    @Getter
    @Setter
    public static class RateLimit {
        /**
         * Минимальный интервал между командами от одного chatId (мс).
         * По умолчанию 1000мс (1 сек).
         */
        private long cooldownMs = 1000;

        /**
         * Размер окна rate-limit (мс).
         * По умолчанию 60000мс (1 мин).
         */
        private long windowMs = 60_000;

        /**
         * Максимум команд за окно.
         * По умолчанию 20.
         */
        private int maxPerWindow = 20;

        /**
         * Максимальный размер кеша состояний (количество chatId).
         * По умолчанию 10000.
         */
        private long maxCacheSize = 10_000;

        /**
         * TTL по неактивности (мс) для состояния chatId.
         * По умолчанию окно + 30 секунд.
         */
        private long expireAfterAccessMs = 90_000;

        /**
         * Минимальный интервал (мс) между сообщениями-предупреждениями
         * о превышении rate-limit для одного chatId.
         *
         * <p>По умолчанию 10000мс (10 сек).</p>
         */
        private long feedbackCooldownMs = 10_000;

    }
}