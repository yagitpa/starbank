package ru.starbank.recommendation.domain.rules.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.starbank.recommendation.domain.rules.entity.RuleQueryEntity;

import java.util.UUID;

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
    private static final Logger log = LoggerFactory.getLogger(QueryEngine.class);

    private final QueryExecutorRegistry registry;

    public QueryEngine(QueryExecutorRegistry registry) {
        this.registry = registry;
    }

    /**
     * Выполнить условие для пользователя с учётом negate.
     */
    public boolean evaluate(UUID userId, RuleQueryEntity query) {
        boolean raw = registry
                .getExecutor(query.getQuery())
                .execute(userId, query);

        boolean result = query.isNegate() != raw;

        log.debug("Query evaluated: user_id={}, queryType={}, negate={}, rawResult={}, result={}",
                userId, query.getQuery(), query.isNegate(), raw, result);
        return result;
    }
}