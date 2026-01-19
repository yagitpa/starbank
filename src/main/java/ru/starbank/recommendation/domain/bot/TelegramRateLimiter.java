package ru.starbank.recommendation.domain.bot;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

/**
 * Простой rate-limit для Telegram-бота на уровне chatId.
 *
 * Ограничения:
 * - не чаще 1 команды в 1 секунду (антиспам/дребезг)
 * - не более 20 команд за 1 минуту на chatId
 */
@Component
public class TelegramRateLimiter {

    private static final Duration COOLDOWN = Duration.ofSeconds(1);
    private static final Duration WINDOW = Duration.ofMinutes(1);
    private static final int MAX_PER_WINDOW = 20;

    private final Cache<Long, WindowState> stateCache = Caffeine.newBuilder()
                                                                .expireAfterAccess(WINDOW.plusSeconds(30)) // чтобы память не росла
                                                                .maximumSize(10_000)
                                                                .build();

    /**
     * @return true если запрос разрешён, false если лимит превышен
     */
    public boolean tryAcquire(long chatId) {
        Instant now = Instant.now();

        WindowState state = stateCache.get(chatId, id -> new WindowState(now, 0, Instant.EPOCH));

        // cooldown: не чаще 1 команды/сек
        assert state != null;
        if (Duration.between(state.lastAcceptedAt, now).compareTo(COOLDOWN) < 0) {
            return false;
        }

        // окно счётчика
        if (Duration.between(state.windowStart, now).compareTo(WINDOW) >= 0) {
            state.windowStart = now;
            state.count = 0;
        }

        if (state.count >= MAX_PER_WINDOW) {
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