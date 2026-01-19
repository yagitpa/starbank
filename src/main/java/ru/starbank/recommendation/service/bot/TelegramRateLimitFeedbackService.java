package ru.starbank.recommendation.service.bot;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Component;
import ru.starbank.recommendation.config.bot.TelegramBotProperties;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * "Мягкий" UX для rate-limit: позволяет отправлять предупреждение
 * "Слишком часто" не чаще одного раза в заданный интервал на chatId.
 */
@Component
public class TelegramRateLimitFeedbackService {

    private final Duration feedbackCooldown;

    private final Cache<Long, Instant> lastFeedbackAt;

    public TelegramRateLimitFeedbackService(TelegramBotProperties properties) {
        Objects.requireNonNull(properties, "properties must not be null");

        TelegramBotProperties.RateLimit rl = properties.getRateLimit();
        this.feedbackCooldown = Duration.ofMillis(rl.getFeedbackCooldownMs());

        this.lastFeedbackAt = Caffeine.newBuilder()
                                      .expireAfterAccess(Duration.ofMinutes(5))
                                      .maximumSize(10_000)
                                      .build();
    }

    /**
     * @return true если можно отправить пользователю предупреждение о rate-limit сейчас
     */
    public boolean shouldSendFeedback(long chatId) {
        Instant now = Instant.now();
        Instant last = lastFeedbackAt.getIfPresent(chatId);

        if (last == null || Duration.between(last, now).compareTo(feedbackCooldown) >= 0) {
            lastFeedbackAt.put(chatId, now);
            return true;
        }
        return false;
    }
}