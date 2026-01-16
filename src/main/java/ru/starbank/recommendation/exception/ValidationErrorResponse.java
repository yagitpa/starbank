package ru.starbank.recommendation.exception;

import java.util.List;

/**
 * Ответ на ошибку валидации данных. Содержит список ошибок.
 */
public record ValidationErrorResponse(String message, List<String> errors) {

    public ValidationErrorResponse(String message) {
        this(message, List.of());
    }

    public void addError(String error) {
        this.errors.add(error);
    }
}