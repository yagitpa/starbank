package ru.starbank.recommendation.domain.bot;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Component;
import ru.starbank.recommendation.config.bot.TelegramBotProperties;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * Rate-limit для Telegram-бота на уровне chatId.
 *
 * Настройки берутся из {@link TelegramBotProperties}.
 */
@Component
public class TelegramRateLimiter {

    private final Duration cooldown;
    private final Duration window;
    private final int maxPerWindow;

    private final Cache<Long, WindowState> stateCache;

    public TelegramRateLimiter(TelegramBotProperties properties) {
        Objects.requireNonNull(properties, "properties must not be null");

        TelegramBotProperties.RateLimit rl = properties.getRateLimit();

        this.cooldown = Duration.ofMillis(rl.getCooldownMs());
        this.window = Duration.ofMillis(rl.getWindowMs());
        this.maxPerWindow = rl.getMaxPerWindow();

        this.stateCache = Caffeine.newBuilder()
                                  .expireAfterAccess(Duration.ofMillis(rl.getExpireAfterAccessMs()))
                                  .maximumSize(rl.getMaxCacheSize())
                                  .build();
    }

    /**
     * @return true если запрос разрешён, false если лимит превышен
     */
    public boolean tryAcquire(long chatId) {
        Instant now = Instant.now();

        WindowState state = stateCache.get(chatId, id -> new WindowState(now, 0, Instant.EPOCH));

        // cooldown: не чаще 1 команды за интервал cooldown
        assert state != null;
        if (Duration.between(state.lastAcceptedAt, now).compareTo(cooldown) < 0) {
            return false;
        }

        // окно счётчика
        if (Duration.between(state.windowStart, now).compareTo(window) >= 0) {
            state.windowStart = now;
            state.count = 0;
        }

        if (state.count >= maxPerWindow) {
            return false;
        }

        state.count++;
        state.lastAcceptedAt = now;
        stateCache.put(chatId, state);
        return true;
    }

    private static final class WindowState {
        private Instant windowStart;
        private int count;
        private Instant lastAcceptedAt;

        private WindowState(Instant windowStart, int count, Instant lastAcceptedAt) {
            this.windowStart = windowStart;
            this.count = count;
            this.lastAcceptedAt = lastAcceptedAt;
        }
    }
}