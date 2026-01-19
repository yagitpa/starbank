package ru.starbank.recommendation.domain.bot;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * Парсер команд Telegram.
 *
 * Поддерживает:
 * - /start
 * - /recommend <username>
 * - /recommend@BotName <username>
 */
public class TelegramCommandParser {

    private final String botUsernameLower;

    public TelegramCommandParser(String botUsername) {
        this.botUsernameLower = Objects.requireNonNull(botUsername, "botUsername must not be null")
                                       .toLowerCase(Locale.ROOT)
                                       .trim();
    }

    public Optional<TelegramCommand> parse(String rawText) {
        if (rawText == null) {
            return Optional.empty();
        }

        String text = rawText.trim();
        if (text.isEmpty() || !text.startsWith("/")) {
            return Optional.empty();
        }

        // Разбиваем на "/command" и "argument..."
        String[] parts = text.split("\\s+", 2);
        String head = parts[0]; // например "/recommend@MyBot"
        String arg = (parts.length > 1) ? parts[1].trim() : null;

        String command = stripSlash(head);
        if (command.isEmpty()) {
            return Optional.empty();
        }

        // Учитываем /cmd@bot
        String name = command;
        int atIdx = command.indexOf('@');
        if (atIdx >= 0) {
            String cmdName = command.substring(0, atIdx);
            String targetBot = command.substring(atIdx + 1);

            // Если команда адресована другому боту — игнорируем
            if (!targetBot.equalsIgnoreCase(botUsernameLower)) {
                return Optional.empty();
            }
            name = cmdName;
        }

        name = name.toLowerCase(Locale.ROOT);

        return Optional.of(new TelegramCommand(name, normalizeArg(arg)));
    }

    private String stripSlash(String head) {
        if (head == null) {
            return "";
        }
        String s = head.trim();
        if (!s.startsWith("/")) {
            return "";
        }
        return s.substring(1).trim();
    }

    private String normalizeArg(String arg) {
        if (arg == null) {
            return null;
        }
        String a = arg.trim();
        return a.isEmpty() ? null : a;
    }
}