package ru.starbank.recommendation.service;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.starbank.recommendation.repository.RuleStatsRepository;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit-тесты для RuleStatsService.increment(ruleId).
 *
 * Проверяем, что сервис вызывает репозиторий и не падает
 * даже если updated != 1 (это будет только WARN лог).
 */
class RuleStatsServiceIncrementTest {

    @Test
    void increment_shouldCallRepositoryIncrement_whenUpdatedIsOne() {
        RuleStatsRepository repo = Mockito.mock(RuleStatsRepository.class);
        when(repo.increment(10L)).thenReturn(1);

        RuleStatsService service = new RuleStatsService(repo);

        assertDoesNotThrow(() -> service.increment(10L));

        verify(repo, times(1)).increment(10L);
        verifyNoMoreInteractions(repo);
    }

    @Test
    void increment_shouldNotThrow_whenUpdatedIsZero() {
        RuleStatsRepository repo = Mockito.mock(RuleStatsRepository.class);
        when(repo.increment(10L)).thenReturn(0);

        RuleStatsService service = new RuleStatsService(repo);

        assertDoesNotThrow(() -> service.increment(10L));

        verify(repo, times(1)).increment(10L);
        verifyNoMoreInteractions(repo);
    }
}