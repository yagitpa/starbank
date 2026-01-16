package ru.starbank.recommendation.domain.rules.engine.executor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.starbank.recommendation.domain.rules.engine.QueryArgumentsParser;
import ru.starbank.recommendation.domain.rules.entity.QueryType;
import ru.starbank.recommendation.domain.rules.entity.RuleQueryEntity;
import ru.starbank.recommendation.repository.KnowledgeRepository;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class TransactionSumCompareExecutorTest {

    @Test
    void execute_shouldCompareSumWithThreshold_usingOperator() {
        KnowledgeRepository knowledgeRepository = Mockito.mock(KnowledgeRepository.class);
        QueryArgumentsParser parser = new QueryArgumentsParser(new ObjectMapper());
        TransactionSumCompareExecutor executor = new TransactionSumCompareExecutor(knowledgeRepository, parser);

        UUID userId = UUID.randomUUID();

        // arguments: [productType, transactionType, operator, amount]
        RuleQueryEntity query = new RuleQueryEntity(
                QueryType.TRANSACTION_SUM_COMPARE,
                "[\"DEBIT\",\"WITHDRAW\",\">\",\"1000\"]",
                false
        );

        when(knowledgeRepository.sumAmount(userId, "DEBIT", "WITHDRAW")).thenReturn(1500L);

        assertTrue(executor.execute(userId, query));
    }

    @Test
    void execute_shouldThrow_whenAmountIsNotNumber() {
        KnowledgeRepository knowledgeRepository = Mockito.mock(KnowledgeRepository.class);
        QueryArgumentsParser parser = new QueryArgumentsParser(new ObjectMapper());
        TransactionSumCompareExecutor executor = new TransactionSumCompareExecutor(knowledgeRepository, parser);

        UUID userId = UUID.randomUUID();

        RuleQueryEntity query = new RuleQueryEntity(
                QueryType.TRANSACTION_SUM_COMPARE,
                "[\"DEBIT\",\"WITHDRAW\",\">\",\"NOT_A_NUMBER\"]",
                false
        );

        when(knowledgeRepository.sumAmount(userId, "DEBIT", "WITHDRAW")).thenReturn(1500L);

        assertThrows(IllegalArgumentException.class, () -> executor.execute(userId, query));
    }
}