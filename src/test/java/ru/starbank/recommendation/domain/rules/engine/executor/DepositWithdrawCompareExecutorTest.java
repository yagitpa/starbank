package ru.starbank.recommendation.domain.rules.engine.executor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import ru.starbank.recommendation.domain.rules.engine.QueryArgumentsParser;
import ru.starbank.recommendation.domain.rules.entity.QueryType;
import ru.starbank.recommendation.domain.rules.entity.RuleQueryEntity;
import ru.starbank.recommendation.repository.KnowledgeRepository;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class DepositWithdrawCompareExecutorTest {

    @Test
    void execute_shouldCompareDepositsAndWithdraws() {
        KnowledgeRepository knowledgeRepository = Mockito.mock(KnowledgeRepository.class);
        QueryArgumentsParser parser = new QueryArgumentsParser(new ObjectMapper());
        DepositWithdrawCompareExecutor executor = new DepositWithdrawCompareExecutor(knowledgeRepository, parser);

        UUID userId = UUID.randomUUID();

        // arguments: [productType, operator]
        RuleQueryEntity query = new RuleQueryEntity(
                QueryType.TRANSACTION_SUM_COMPARE_DEPOSIT_WITHDRAW,
                "[\"SAVING\",\">=\"]",
                false
        );

        when(knowledgeRepository.sumAmount(userId, "SAVING", "DEPOSIT")).thenReturn(5000L);
        when(knowledgeRepository.sumAmount(userId, "SAVING", "WITHDRAW")).thenReturn(5000L);

        assertTrue(executor.execute(userId, query));
    }
}