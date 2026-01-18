package ru.starbank.recommendation.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.starbank.recommendation.domain.dto.RecommendationDto;
import ru.starbank.recommendation.domain.dto.RecommendationResponseDto;
import ru.starbank.recommendation.repository.UserLookupRepository;

import java.util.List;
import java.util.Objects;

/**
 * Telegram Bot (Stage 3).
 *
 * <p>Поддерживает команды:
 * <ul>
 *     <li>/start — приветствие и справка</li>
 *     <li>/recommend &lt;username&gt; — рекомендации для пользователя</li>
 * </ul>
 * </p>
 */
@Service
public class TelegramBotService extends TelegramLongPollingBot {

    private static final Logger log = LoggerFactory.getLogger(TelegramBotService.class);

    private final RecommendationService recommendationService;
    private final UserLookupRepository userLookupRepository;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.username}")
    private String botUsername;

    public TelegramBotService(RecommendationService recommendationService,
                              UserLookupRepository userLookupRepository) {
        this.recommendationService = Objects.requireNonNull(recommendationService, "recommendationService must not be null");
        this.userLookupRepository = Objects.requireNonNull(userLookupRepository, "userLookupRepository must not be null");
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }

        String text = update.getMessage().getText().trim();
        Long chatId = update.getMessage().getChatId();

        if ("/start".equals(text)) {
            sendHelpMessage(chatId);
            return;
        }

        if (text.startsWith("/recommend")) {
            handleRecommendCommand(chatId, text);
            return;
        }

        // Неизвестные сообщения/команды
        sendMessage(chatId, "Команда не распознана. Используйте /start для справки.");
    }

    private void handleRecommendCommand(Long chatId, String text) {
        String username = extractUsername(text);

        if (username == null || username.isBlank()) {
            // ТЗ строго требует фразу для сценария "не найден" (и при ошибочной команде тоже).
            sendMessage(chatId, "Пользователь не найден");
            return;
        }

        List<UserLookupRepository.BankUserRow> users = userLookupRepository.findByUsername(username);

        // По ТЗ: 0 результатов -> "Пользователь не найден", >1 -> тоже "Пользователь не найден"
        if (users.size() != 1) {
            sendMessage(chatId, "Пользователь не найден");
            return;
        }

        UserLookupRepository.BankUserRow user = users.get(0);

        log.info("Bot: recommendations requested. chat_id={}, username={}, user_id={}",
                chatId, username, user.id());

        RecommendationResponseDto response;
        try {
            response = recommendationService.getRecommendations(user.id());
        } catch (Exception e) {
            log.error("Bot: error while getting recommendations. chat_id={}, user_id={}", chatId, user.id(), e);
            sendMessage(chatId, "Ошибка при получении рекомендаций, попробуйте позже.");
            return;
        }

        String message = formatRecommendationsMessage(user.firstName(), user.lastName(), response.recommendations());
        sendMessage(chatId, message);
    }

    private String extractUsername(String text) {
        // Ожидаем формат: /recommend username
        String[] parts = text.split("\\s+", 2);
        if (parts.length < 2) {
            return null;
        }
        return parts[1].trim();
    }

    private String formatRecommendationsMessage(String firstName, String lastName, List<RecommendationDto> recommendations) {
        StringBuilder sb = new StringBuilder();

        sb.append("Здравствуйте ")
          .append(firstName)
          .append(" ")
          .append(lastName)
          .append("\n");

        sb.append("Новые продукты для вас:")
          .append("\n");

        for (RecommendationDto r : recommendations) {
            sb.append("• ").append(r.name()).append("\n");
        }

        return sb.toString().trim();
    }

    private void sendMessage(Long chatId, String text) {
        SendMessage message = new SendMessage(chatId.toString(), text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Bot: failed to send message. chat_id={}", chatId, e);
        }
    }

    private void sendHelpMessage(Long chatId) {
        String welcomeMessage = "Здравствуйте! Я ваш виртуальный помощник.\n\n";
        String helpMessage = "Доступные команды:\n" +
                "/recommend <username> — получите рекомендации для пользователя.\n\n" +
                "Просто напишите мне команду!";
        sendMessage(chatId, welcomeMessage + helpMessage);
    }
}