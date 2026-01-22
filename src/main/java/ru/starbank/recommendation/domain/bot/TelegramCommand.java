package ru.starbank.recommendation.domain.bot;

/**
 * Распознанная команда Telegram.
 *
 * @param name имя команды без '/'
 * @param argument строковый аргумент (например, username), может быть null
 */
public record TelegramCommand(
        String name,
        String argument
) {
}