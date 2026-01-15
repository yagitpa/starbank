package ru.starbank.recommendation.domain.rules.engine.executor;

import org.springframework.stereotype.Component;
import ru.starbank.recommendation.domain.rules.entity.QueryType;
import ru.starbank.recommendation.domain.rules.entity.RuleQueryEntity;
import ru.starbank.recommendation.repository.KnowledgeRepository;
import ru.starbank.recommendation.domain.rules.engine.QueryArgumentsParser;
import ru.starbank.recommendation.domain.rules.engine.TypedQueryExecutor;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * ACTIVE_USER_OF: COUNT(transactions) >= 5 по продукту типа X.
 */
@Component
public class ActiveUserOfQueryExecutor implements TypedQueryExecutor {

    private static final long ACTIVE_THRESHOLD = 5;

    private final KnowledgeRepository knowledgeRepository;
    private final QueryArgumentsParser argumentsParser;

    public ActiveUserOfQueryExecutor(KnowledgeRepository knowledgeRepository, QueryArgumentsParser argumentsParser) {
        this.knowledgeRepository = Objects.requireNonNull(knowledgeRepository, "knowledgeRepository must not be null");
        this.argumentsParser = Objects.requireNonNull(argumentsParser, "argumentsParser must not be null");
    }

    @Override
    public QueryType supportedType() {
        return QueryType.ACTIVE_USER_OF;
    }

    @Override
    public boolean execute(UUID userId, RuleQueryEntity query) {
        List<String> args = argumentsParser.parse(query.getArguments());
        String productType = argumentsParser.requireAt(args, 0, "productType");
        return knowledgeRepository.countTransactions(userId, productType) >= ACTIVE_THRESHOLD;
    }
}
