package ru.starbank.recommendation.domain.rules.engine;

import ru.starbank.recommendation.domain.rules.entity.RuleQueryEntity;

/**
 * Исполнитель одного типа query.
 *
 * <p>Каждая реализация отвечает за ОДИН {@link ru.starbank.recommendation.domain.rules.entity.QueryType}.</p>
 */
public interface QueryExecutor {

    /**
     * Выполнить условие для конкретного пользователя.
     *
     * @param userId id пользователя
     * @param query  сущность условия
     * @return результат выполнения (до применения negate)
     */
    boolean execute(long userId, RuleQueryEntity query);
}