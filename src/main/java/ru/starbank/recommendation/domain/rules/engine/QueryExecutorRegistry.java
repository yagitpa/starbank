package ru.starbank.recommendation.domain.rules.engine;

import org.springframework.stereotype.Component;
import ru.starbank.recommendation.domain.rules.entity.QueryType;
import ru.starbank.recommendation.exception.UnsupportedQueryTypeException;

import java.util.EnumMap;
import java.util.Map;

/**
 * Реестр исполнителей запросов.
 *
 * <p>Связывает {@link QueryType} с соответствующим {@link QueryExecutor}.</p>
 */
@Component
public class QueryExecutorRegistry {

    private final Map<QueryType, QueryExecutor> executors = new EnumMap<>(QueryType.class);

    public QueryExecutorRegistry(Map<String, QueryExecutor> beans) {
        for (QueryExecutor executor : beans.values()) {
            if (executor instanceof TypedQueryExecutor typed) {
                executors.put(typed.supportedType(), executor);
            }
        }
    }

    public QueryExecutor getExecutor(QueryType type) {
        QueryExecutor executor = executors.get(type);
        if (executor == null) {
            throw new UnsupportedQueryTypeException(type);
        }
        return executor;
    }
}