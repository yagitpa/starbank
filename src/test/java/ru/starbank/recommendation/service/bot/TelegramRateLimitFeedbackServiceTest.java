package ru.starbank.recommendation.service.bot;

import org.junit.jupiter.api.Test;
import ru.starbank.recommendation.config.bot.TelegramBotProperties;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TelegramRateLimitFeedbackServiceTest {

    @Test
    void shouldSendFeedback_shouldAllowFirstTime() {
        TelegramBotProperties props = new TelegramBotProperties();
        props.getRateLimit().setFeedbackCooldownMs(10_000);

        TelegramRateLimitFeedbackService svc = new TelegramRateLimitFeedbackService(props);

        assertTrue(svc.shouldSendFeedback(1L));
    }

    @Test
    void shouldSendFeedback_shouldBlockWithinCooldown() {
        TelegramBotProperties props = new TelegramBotProperties();
        props.getRateLimit().setFeedbackCooldownMs(60_000); // большой cooldown

        TelegramRateLimitFeedbackService svc = new TelegramRateLimitFeedbackService(props);

        assertTrue(svc.shouldSendFeedback(1L));
        assertFalse(svc.shouldSendFeedback(1L)); // сразу второй раз — блок
    }

    @Test
    void shouldSendFeedback_shouldBeIndependentPerChat() {
        TelegramBotProperties props = new TelegramBotProperties();
        props.getRateLimit().setFeedbackCooldownMs(60_000);

        TelegramRateLimitFeedbackService svc = new TelegramRateLimitFeedbackService(props);

        assertTrue(svc.shouldSendFeedback(1L));
        assertTrue(svc.shouldSendFeedback(2L)); // другой чат — разрешено
    }
}