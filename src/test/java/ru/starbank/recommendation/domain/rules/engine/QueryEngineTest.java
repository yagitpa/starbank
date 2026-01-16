package ru.starbank.recommendation.domain.rules.engine;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.starbank.recommendation.domain.rules.entity.QueryType;
import ru.starbank.recommendation.domain.rules.entity.RuleQueryEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class QueryEngineTest {

    @Test
    void evaluate_shouldApplyNegateFalse_asIs() {
        QueryExecutorRegistry registry = Mockito.mock(QueryExecutorRegistry.class);
        QueryExecutor executor = Mockito.mock(QueryExecutor.class);

        when(registry.getExecutor(QueryType.USER_OF)).thenReturn(executor);
        when(executor.execute(any(UUID.class), any(RuleQueryEntity.class))).thenReturn(true);

        QueryEngine engine = new QueryEngine(registry);

        RuleQueryEntity q = new RuleQueryEntity(QueryType.USER_OF, "[\"DEBIT\"]", false);

        boolean result = engine.evaluate(UUID.randomUUID(), q);
        assertTrue(result);
    }

    @Test
    void evaluate_shouldInvertResult_whenNegateTrue() {
        QueryExecutorRegistry registry = Mockito.mock(QueryExecutorRegistry.class);
        QueryExecutor executor = Mockito.mock(QueryExecutor.class);

        when(registry.getExecutor(QueryType.USER_OF)).thenReturn(executor);
        when(executor.execute(any(UUID.class), any(RuleQueryEntity.class))).thenReturn(true);

        QueryEngine engine = new QueryEngine(registry);

        RuleQueryEntity q = new RuleQueryEntity(QueryType.USER_OF, "[\"DEBIT\"]", true);

        boolean result = engine.evaluate(UUID.randomUUID(), q);
        assertFalse(result);
    }
}