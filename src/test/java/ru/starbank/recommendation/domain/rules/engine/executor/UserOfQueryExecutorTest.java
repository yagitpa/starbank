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

class UserOfQueryExecutorTest {

    @Test
    void execute_shouldCallKnowledgeRepository_andReturnItsResult() {
        KnowledgeRepository knowledgeRepository = Mockito.mock(KnowledgeRepository.class);
        QueryArgumentsParser parser = new QueryArgumentsParser(new ObjectMapper());
        UserOfQueryExecutor executor = new UserOfQueryExecutor(knowledgeRepository, parser);

        UUID userId = UUID.randomUUID();
        RuleQueryEntity query = new RuleQueryEntity(QueryType.USER_OF, "[\"DEBIT\"]", false);

        when(knowledgeRepository.hasAnyTransaction(userId, "DEBIT")).thenReturn(true);

        boolean result = executor.execute(userId, query);

        assertTrue(result);
    }
}