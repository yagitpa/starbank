package ru.starbank.recommendation.exception;

/**
 * Некорректный product_id в динамическом правиле.
 */
public class InvalidProductIdException extends RuntimeException {

    public InvalidProductIdException(String productId, Throwable cause) {
        super("Некорректный product_id в динамическом правиле. Ожидался UUID, получено: " + productId, cause);
    }
}