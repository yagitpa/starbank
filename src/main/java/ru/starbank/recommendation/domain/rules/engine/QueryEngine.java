package ru.starbank.recommendation.domain.rules.engine;

import org.springframework.stereotype.Component;
import ru.starbank.recommendation.domain.rules.entity.RuleQueryEntity;

/**
 * Движок выполнения динамических условий.
 *
 * <p>Отвечает за:
 * <ul>
 *   <li>выбор нужного исполнителя</li>
 *   <li>применение negate</li>
 * </ul>
 */
@Component
public class QueryEngine {

    private final QueryExecutorRegistry registry;

    public QueryEngine(QueryExecutorRegistry registry) {
        this.registry = registry;
    }

    /**
     * Выполнить условие для пользователя с учётом negate.
     */
    public boolean evaluate(long userId, RuleQueryEntity query) {
        boolean result = registry
                .getExecutor(query.getQuery())
                .execute(userId, query);

        return query.isNegate() != result;
    }
}