package ru.starbank.recommendation.support.exception;

/**
 * Exception thrown when user_id cannot be parsed as UUID.
 */
public class InvalidUserIdException extends RuntimeException {

    /**
     * Constructor.
     *
     * @param message message
     * @param cause cause
     */
    public InvalidUserIdException(String message, Throwable cause) {
        super(message, cause);
    }
}