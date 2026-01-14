package ru.starbank.recommendation.support.exception;

/**
 * Исключение, возникающее, когда пользователь не найден в базе данных.
 */
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}