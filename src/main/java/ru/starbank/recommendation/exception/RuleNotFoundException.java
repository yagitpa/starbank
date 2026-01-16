package ru.starbank.recommendation.exception;

/**
 * Динамическое правило не найдено.
 */
public class RuleNotFoundException extends RuntimeException {

    public RuleNotFoundException(long id) {
        super("Правило с id=" + id + " не найдено");
    }
}