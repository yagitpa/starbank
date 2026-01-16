package ru.starbank.recommendation.domain.rules.engine.executor;

import org.springframework.stereotype.Component;
import ru.starbank.recommendation.domain.rules.engine.QueryArgumentsParser;
import ru.starbank.recommendation.domain.rules.engine.TypedQueryExecutor;
import ru.starbank.recommendation.domain.rules.entity.QueryType;
import ru.starbank.recommendation.domain.rules.entity.RuleQueryEntity;
import ru.starbank.recommendation.repository.KnowledgeRepository;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * USER_OF: наличие хотя бы одной транзакции по продукту типа X.
 */
@Component
public class UserOfQueryExecutor implements TypedQueryExecutor {

    private final KnowledgeRepository knowledgeRepository;
    private final QueryArgumentsParser argumentsParser;

    public UserOfQueryExecutor(KnowledgeRepository knowledgeRepository, QueryArgumentsParser argumentsParser) {
        this.knowledgeRepository = Objects.requireNonNull(knowledgeRepository, "knowledgeRepository must not be null");
        this.argumentsParser = Objects.requireNonNull(argumentsParser, "argumentsParser must not be null");
    }

    @Override
    public QueryType supportedType() {
        return QueryType.USER_OF;
    }

    @Override
    public boolean execute(UUID userId, RuleQueryEntity query) {
        List<String> args = argumentsParser.parse(query.getArguments());
        String productType = argumentsParser.requireAt(args, 0, "productType");
        return knowledgeRepository.hasAnyTransaction(userId, productType);
    }
}