package ru.starbank.recommendation.service.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import ru.starbank.recommendation.domain.bot.TelegramCommand;
import ru.starbank.recommendation.domain.bot.TelegramCommandParser;
import ru.starbank.recommendation.domain.bot.TelegramRateLimiter;
import ru.starbank.recommendation.domain.dto.RecommendationDto;
import ru.starbank.recommendation.domain.dto.RecommendationResponseDto;
import ru.starbank.recommendation.repository.jdbc.UserLookupRepository;
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
    private final TelegramRateLimitFeedbackService rateLimitFeedbackService;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.username}")
    private String botUsername;

    public TelegramBotService(RecommendationService recommendationService,
                              UserLookupRepository userLookupRepository,
                              TelegramRateLimiter rateLimiter,
                              TelegramRateLimitFeedbackService rateLimitFeedbackService) {
        this.recommendationService = Objects.requireNonNull(recommendationService, "recommendationService must not be null");
        this.userLookupRepository = Objects.requireNonNull(userLookupRepository, "userLookupRepository must not be null");
        this.rateLimiter = Objects.requireNonNull(rateLimiter, "rateLimiter must not be null");
        this.rateLimitFeedbackService = Objects.requireNonNull(rateLimitFeedbackService, "rateLimitFeedbackService must not be null");
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
        User from = update.getMessage().getFrom();

        String fromUsername = (from != null) ? from.getUserName() : null;
        Long fromId = (from != null) ? from.getId() : null;

        log.debug("Bot: update received. chat_id={}, from_id={}, from_username={}, text='{}'",
                chatId, fromId, fromUsername, rawText);

        if (!rateLimiter.tryAcquire(chatId)) {
            log.debug("Bot: rate limit exceeded. chat_id={}, from_id={}", chatId, fromId);

            // "Мягкий" UX: предупреждаем редко, чтобы не спамить
            if (rateLimitFeedbackService.shouldSendFeedback(chatId)) {
                sendMessage(chatId, "Слишком часто. Подождите немного и повторите команду.");
            }
            return;
        }

        TelegramCommandParser parser = new TelegramCommandParser(getBotUsername());
        Optional<TelegramCommand> cmdOpt = parser.parse(rawText);

        if (cmdOpt.isEmpty()) {
            log.debug("Bot: non-command message. chat_id={}, from_id={}", chatId, fromId);
            sendMessage(chatId, "Команда не распознана. Используйте /start для справки.");
            return;
        }

        TelegramCommand cmd = cmdOpt.get();
        log.info("Bot: command parsed. chat_id={}, from_id={}, command={}, arg={}",
                chatId, fromId, cmd.name(), cmd.argument());

        switch (cmd.name()) {
            case "start" -> {
                log.info("Bot: /start. chat_id={}, from_id={}", chatId, fromId);
                sendHelpMessage(chatId);
            }
            case "recommend" -> {
                log.info("Bot: /recommend received. chat_id={}, from_id={}, arg={}", chatId, fromId, cmd.argument());
                handleRecommendCommand(chatId, cmd.argument(), fromId);
            }
            default -> {
                log.info("Bot: unknown command. chat_id={}, from_id={}, command={}", chatId, fromId, cmd.name());
                sendMessage(chatId, "Команда не распознана. Используйте /start для справки.");
            }
        }
    }

    private void handleRecommendCommand(Long chatId, String usernameArg, Long fromId) {
        if (usernameArg == null || usernameArg.isBlank()) {
            log.info("Bot: /recommend missing username. chat_id={}, from_id={}", chatId, fromId);
            sendMessage(chatId, "Пользователь не найден");
            return;
        }

        String username = usernameArg.trim();
        log.debug("Bot: lookup user by username. chat_id={}, from_id={}, username={}", chatId, fromId, username);

        List<UserLookupRepository.BankUserRow> users = userLookupRepository.findByUsername(username);
        log.debug("Bot: user lookup result. chat_id={}, from_id={}, username={}, found={}",
                chatId, fromId, username, users.size());

        if (users.size() != 1) {
            log.info("Bot: user not found or ambiguous. chat_id={}, from_id={}, username={}, found={}",
                    chatId, fromId, username, users.size());
            sendMessage(chatId, "Пользователь не найден");
            return;
        }

        UserLookupRepository.BankUserRow user = users.get(0);
        log.info("Bot: user resolved. chat_id={}, from_id={}, username={}, user_id={}, name='{} {}'",
                chatId, fromId, username, user.id(), user.firstName(), user.lastName());

        RecommendationResponseDto response;
        try {
            response = recommendationService.getRecommendations(user.id());
        } catch (Exception e) {
            log.error("Bot: error while getting recommendations. chat_id={}, from_id={}, user_id={}",
                    chatId, fromId, user.id(), e);
            sendMessage(chatId, "Ошибка при получении рекомендаций, попробуйте позже.");
            return;
        }

        List<RecommendationDto> recs = response.recommendations();
        log.info("Bot: recommendations computed. chat_id={}, from_id={}, user_id={}, count={}",
                chatId, fromId, user.id(), recs.size());

        String message = formatRecommendationsMessage(user.firstName(), user.lastName(), recs);
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
            log.debug("Bot: message sent. chat_id={}, length={}", chatId, text != null ? text.length() : 0);
        } catch (TelegramApiRequestException e) {
            int code = e.getErrorCode();
            String apiResponse = e.getApiResponse();

            if (code == 401) {
                log.error("Bot: send failed (401 Unauthorized). Проверь TELEGRAM_BOT_TOKEN. chat_id={}, apiResponse={}",
                        chatId, apiResponse);
            } else if (code == 403) {
                log.warn("Bot: send failed (403 Forbidden). User may have blocked the bot. chat_id={}, apiResponse={}",
                        chatId, apiResponse);
            } else {
                log.error("Bot: send failed. chat_id={}, errorCode={}, apiResponse={}",
                        chatId, code, apiResponse, e);
            }
        } catch (TelegramApiException e) {
            log.error("Bot: failed to send message (TelegramApiException). chat_id={}", chatId, e);
        }
    }
}