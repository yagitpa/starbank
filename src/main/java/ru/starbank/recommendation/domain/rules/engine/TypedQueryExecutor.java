package ru.starbank.recommendation.domain.rules.engine;

import ru.starbank.recommendation.domain.rules.entity.QueryType;

/**
 * Расширение {@link QueryExecutor}, позволяющее определить поддерживаемый {@link QueryType}.
 */
public interface TypedQueryExecutor extends QueryExecutor {

    QueryType supportedType();
}