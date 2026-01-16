package ru.starbank.recommendation.exception;

/**
 * Некорректные arguments у динамического правила (например, не JSON или неверный формат).
 */
public class InvalidRuleArgumentsException extends RuntimeException {

    public InvalidRuleArgumentsException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidRuleArgumentsException(String message) {
        super(message);
    }
}