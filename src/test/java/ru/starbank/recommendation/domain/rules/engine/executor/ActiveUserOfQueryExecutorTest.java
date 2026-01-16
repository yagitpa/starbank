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

class ActiveUserOfQueryExecutorTest {

    @Test
    void execute_shouldReturnTrue_whenCountIsAtLeast5() {
        KnowledgeRepository knowledgeRepository = Mockito.mock(KnowledgeRepository.class);
        QueryArgumentsParser parser = new QueryArgumentsParser(new ObjectMapper());
        ActiveUserOfQueryExecutor executor = new ActiveUserOfQueryExecutor(knowledgeRepository, parser);

        UUID userId = UUID.randomUUID();
        RuleQueryEntity query = new RuleQueryEntity(QueryType.ACTIVE_USER_OF, "[\"DEBIT\"]", false);

        when(knowledgeRepository.countTransactions(userId, "DEBIT")).thenReturn(5L);

        assertTrue(executor.execute(userId, query));
    }

    @Test
    void execute_shouldReturnFalse_whenCountIsLessThan5() {
        KnowledgeRepository knowledgeRepository = Mockito.mock(KnowledgeRepository.class);
        QueryArgumentsParser parser = new QueryArgumentsParser(new ObjectMapper());
        ActiveUserOfQueryExecutor executor = new ActiveUserOfQueryExecutor(knowledgeRepository, parser);

        UUID userId = UUID.randomUUID();
        RuleQueryEntity query = new RuleQueryEntity(QueryType.ACTIVE_USER_OF, "[\"DEBIT\"]", false);

        when(knowledgeRepository.countTransactions(userId, "DEBIT")).thenReturn(4L);

        assertFalse(executor.execute(userId, query));
    }
}