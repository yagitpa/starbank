package ru.starbank.recommendation.domain.rules.engine.executor;

import org.springframework.stereotype.Component;
import ru.starbank.recommendation.domain.rules.engine.ComparisonOperator;
import ru.starbank.recommendation.domain.rules.engine.QueryArgumentsParser;
import ru.starbank.recommendation.domain.rules.engine.TypedQueryExecutor;
import ru.starbank.recommendation.domain.rules.entity.QueryType;
import ru.starbank.recommendation.domain.rules.entity.RuleQueryEntity;
import ru.starbank.recommendation.repository.KnowledgeRepository;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * TRANSACTION_SUM_COMPARE_DEPOSIT_WITHDRAW:
 * Считает две суммы (DEPOSIT и WITHDRAW) по (user_id, product_type=X),
 * затем сравнивает sumDeposit и sumWithdraw оператором O.
 *
 * arguments: [productType, operator]
 */
@Component
public class DepositWithdrawCompareExecutor implements TypedQueryExecutor {

    private static final String DEPOSIT = "DEPOSIT";
    private static final String WITHDRAW = "WITHDRAW";

    private final KnowledgeRepository knowledgeRepository;
    private final QueryArgumentsParser argumentsParser;

    public DepositWithdrawCompareExecutor(KnowledgeRepository knowledgeRepository, QueryArgumentsParser argumentsParser) {
        this.knowledgeRepository = Objects.requireNonNull(knowledgeRepository, "knowledgeRepository must not be null");
        this.argumentsParser = Objects.requireNonNull(argumentsParser, "argumentsParser must not be null");
    }

    @Override
    public QueryType supportedType() {
        return QueryType.TRANSACTION_SUM_COMPARE_DEPOSIT_WITHDRAW;
    }

    @Override
    public boolean execute(UUID userId, RuleQueryEntity query) {
        List<String> args = argumentsParser.parse(query.getArguments());

        String productType = argumentsParser.requireAt(args, 0, "productType");
        String operatorToken = argumentsParser.requireAt(args, 1, "operator");

        long deposits = knowledgeRepository.sumAmount(userId, productType, DEPOSIT);
        long withdraws = knowledgeRepository.sumAmount(userId, productType, WITHDRAW);

        ComparisonOperator op = ComparisonOperator.fromToken(operatorToken);
        return op.apply(deposits, withdraws);
    }
}