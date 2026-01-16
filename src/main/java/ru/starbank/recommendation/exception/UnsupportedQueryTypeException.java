package ru.starbank.recommendation.exception;

import ru.starbank.recommendation.domain.rules.entity.QueryType;

/**
 * Для QueryType не найден исполнитель (executor).
 */
public class UnsupportedQueryTypeException extends RuntimeException {

    public UnsupportedQueryTypeException(QueryType type) {
        super("Не поддерживается тип условия (нет executor): " + type);
    }
}