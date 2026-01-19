package ru.starbank.recommendation.service.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.starbank.recommendation.domain.bot.TelegramCommand;
import ru.starbank.recommendation.domain.bot.TelegramCommandParser;
import ru.starbank.recommendation.domain.bot.TelegramRateLimiter;
import ru.starbank.recommendation.domain.dto.RecommendationDto;
import ru.starbank.recommendation.domain.dto.RecommendationResponseDto;
import ru.starbank.recommendation.repository.UserLookupRepository;
import ru.starbank.recommendation.service.RecommendationService;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Telegram Bot (Stage 3).
 *
 * Команды:
 * - /start — приветствие и справка
 * - /recommend <username> — рекомендации
 *
 * Требования ТЗ:
 * - если 0 или >1 пользователей — строго "Пользователь не найден"
 * - успешный ответ содержит "Здравствуйте <Имя Фамилия>" и "Новые продукты для вас:"
 */
@Service
public class TelegramBotService extends TelegramLongPollingBot {

    private static final Logger log = LoggerFactory.getLogger(TelegramBotService.class);

    private final RecommendationService recommendationService;
    private final UserLookupRepository userLookupRepository;
    private final TelegramRateLimiter rateLimiter;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.username}")
    private String botUsername;

    public TelegramBotService(RecommendationService recommendationService,
                              UserLookupRepository userLookupRepository,
                              TelegramRateLimiter rateLimiter) {
        this.recommendationService = Objects.requireNonNull(recommendationService, "recommendationService must not be null");
        this.userLookupRepository = Objects.requireNonNull(userLookupRepository, "userLookupRepository must not be null");
        this.rateLimiter = Objects.requireNonNull(rateLimiter, "rateLimiter must not be null");
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

        Long chatId = update.getMessage().getChatId();
        String rawText = update.getMessage().getText();

        // Rate-limit только на команды/текстовые сообщения
        if (!rateLimiter.tryAcquire(chatId)) {
            // Молчаливое ограничение (не спамим ответами).
            log.debug("Bot: rate limit exceeded. chat_id={}", chatId);
            return;
        }

        TelegramCommandParser parser = new TelegramCommandParser(getBotUsername());
        Optional<TelegramCommand> cmdOpt = parser.parse(rawText);

        if (cmdOpt.isEmpty()) {
            // Не команда — по PoC подскажем как пользоваться
            sendMessage(chatId, "Команда не распознана. Используйте /start для справки.");
            return;
        }

        TelegramCommand cmd = cmdOpt.get();
        switch (cmd.name()) {
            case "start" -> sendHelpMessage(chatId);
            case "recommend" -> handleRecommendCommand(chatId, cmd.argument());
            default -> sendMessage(chatId, "Команда не распознана. Используйте /start для справки.");
        }
    }

    private void handleRecommendCommand(Long chatId, String usernameArg) {
        if (usernameArg == null || usernameArg.isBlank()) {
            // По ТЗ: если не можем определить пользователя — "Пользователь не найден"
            sendMessage(chatId, "Пользователь не найден");
            return;
        }

        String username = usernameArg.trim();

        List<UserLookupRepository.BankUserRow> users = userLookupRepository.findByUsername(username);

        // По ТЗ: 0 результатов -> "Пользователь не найден", >1 -> тоже "Пользователь не найден"
        if (users.size() != 1) {
            sendMessage(chatId, "Пользователь не найден");
            return;
        }

        UserLookupRepository.BankUserRow user = users.get(0);

        log.info("Bot: /recommend. chat_id={}, username={}, user_id={}", chatId, username, user.id());

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

    private void sendHelpMessage(Long chatId) {
        String welcomeMessage = "Здравствуйте! Я ваш виртуальный помощник.\n\n";
        String helpMessage = "Доступные команды:\n" +
                "/recommend <username> — получите рекомендации для пользователя.\n\n" +
                "Просто напишите мне команду!";
        sendMessage(chatId, welcomeMessage + helpMessage);
    }

    private void sendMessage(Long chatId, String text) {
        SendMessage message = new SendMessage(chatId.toString(), text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Bot: failed to send message. chat_id={}", chatId, e);
        }
    }
}