package ru.starbank.recommendation.domain.bot;

import org.junit.jupiter.api.Test;
import ru.starbank.recommendation.config.bot.TelegramBotProperties;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TelegramRateLimiterTest {

    @Test
    void tryAcquire_shouldAllowFirstRequest() {
        TelegramBotProperties props = new TelegramBotProperties();
        props.getRateLimit().setCooldownMs(1000);
        props.getRateLimit().setWindowMs(60_000);
        props.getRateLimit().setMaxPerWindow(20);

        TelegramRateLimiter limiter = new TelegramRateLimiter(props);

        assertTrue(limiter.tryAcquire(1L));
    }

    @Test
    void tryAcquire_shouldBlockDuringCooldown() {
        TelegramBotProperties props = new TelegramBotProperties();
        props.getRateLimit().setCooldownMs(10_000); // большой cooldown
        props.getRateLimit().setWindowMs(60_000);
        props.getRateLimit().setMaxPerWindow(20);

        TelegramRateLimiter limiter = new TelegramRateLimiter(props);

        assertTrue(limiter.tryAcquire(1L));
        assertFalse(limiter.tryAcquire(1L)); // сразу второй раз — должен заблокировать
    }

    @Test
    void tryAcquire_shouldBlockAfterMaxPerWindow() {
        TelegramBotProperties props = new TelegramBotProperties();
        props.getRateLimit().setCooldownMs(0); // выключаем cooldown для теста окна
        props.getRateLimit().setWindowMs(60_000);
        props.getRateLimit().setMaxPerWindow(3);

        TelegramRateLimiter limiter = new TelegramRateLimiter(props);

        assertTrue(limiter.tryAcquire(1L));
        assertTrue(limiter.tryAcquire(1L));
        assertTrue(limiter.tryAcquire(1L));
        assertFalse(limiter.tryAcquire(1L)); // 4-й запрос должен быть заблокирован
    }

    @Test
    void tryAcquire_shouldBeIndependentPerChat() {
        TelegramBotProperties props = new TelegramBotProperties();
        props.getRateLimit().setCooldownMs(0);
        props.getRateLimit().setWindowMs(60_000);
        props.getRateLimit().setMaxPerWindow(1);

        TelegramRateLimiter limiter = new TelegramRateLimiter(props);

        assertTrue(limiter.tryAcquire(1L));
        assertFalse(limiter.tryAcquire(1L));

        assertTrue(limiter.tryAcquire(2L)); // другой chatId — должен пройти
        assertFalse(limiter.tryAcquire(2L));
    }
}