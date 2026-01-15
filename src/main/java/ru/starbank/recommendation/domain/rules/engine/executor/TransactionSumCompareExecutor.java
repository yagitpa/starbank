package ru.starbank.recommendation.domain.rules.engine.executor;

import org.springframework.stereotype.Component;
import ru.starbank.recommendation.domain.rules.entity.QueryType;
import ru.starbank.recommendation.domain.rules.entity.RuleQueryEntity;
import ru.starbank.recommendation.repository.KnowledgeRepository;
import ru.starbank.recommendation.domain.rules.engine.ComparisonOperator;
import ru.starbank.recommendation.domain.rules.engine.QueryArgumentsParser;
import ru.starbank.recommendation.domain.rules.engine.TypedQueryExecutor;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * TRANSACTION_SUM_COMPARE:
 * SUM(amount) по (user_id, product_type=X, transaction_type=Y) сравнивается с константой C оператором O.
 *
 * arguments: [productType, transactionType, operator, amount]
 */
@Component
public class TransactionSumCompareExecutor implements TypedQueryExecutor {

    private final KnowledgeRepository knowledgeRepository;
    private final QueryArgumentsParser argumentsParser;

    public TransactionSumCompareExecutor(KnowledgeRepository knowledgeRepository, QueryArgumentsParser argumentsParser) {
        this.knowledgeRepository = Objects.requireNonNull(knowledgeRepository, "knowledgeRepository must not be null");
        this.argumentsParser = Objects.requireNonNull(argumentsParser, "argumentsParser must not be null");
    }

    @Override
    public QueryType supportedType() {
        return QueryType.TRANSACTION_SUM_COMPARE;
    }

    @Override
    public boolean execute(UUID userId, RuleQueryEntity query) {
        List<String> args = argumentsParser.parse(query.getArguments());

        String productType = argumentsParser.requireAt(args, 0, "productType");
        String transactionType = argumentsParser.requireAt(args, 1, "transactionType");
        String operatorToken = argumentsParser.requireAt(args, 2, "operator");
        String amountStr = argumentsParser.requireAt(args, 3, "amount");

        long sum = knowledgeRepository.sumAmount(userId, productType, transactionType);
        long threshold = parseLong(amountStr, "amount");
        ComparisonOperator op = ComparisonOperator.fromToken(operatorToken);

        return op.apply(sum, threshold);
    }

    private long parseLong(String value, String name) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Некорректное число для " + name + ": " + value, e);
        }
    }
}