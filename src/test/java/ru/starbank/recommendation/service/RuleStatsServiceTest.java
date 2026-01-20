package ru.starbank.recommendation.service;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.starbank.recommendation.domain.dto.rule.RuleStatsResponseDto;
import ru.starbank.recommendation.repository.jpa.RuleStatsRepository;
import ru.starbank.recommendation.service.dynamic.RuleStatsService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit-тесты RuleStatsService (Stage 3).
 *
 * Без Spring, без БД — только проверяем:
 * - корректный маппинг projection -> DTO
 * - корректную обработку пустого списка
 */
class RuleStatsServiceTest {

    @Test
    void getStats_shouldMapProjectionsToDto() {
        RuleStatsRepository repo = Mockito.mock(RuleStatsRepository.class);

        RuleStatsRepository.RuleStatProjection p1 = new ProjectionStub(1L, 3L);
        RuleStatsRepository.RuleStatProjection p2 = new ProjectionStub(2L, 0L);

        when(repo.findAllRuleStats()).thenReturn(List.of(p1, p2));

        RuleStatsService service = new RuleStatsService(repo);

        RuleStatsResponseDto dto = service.getStats();

        assertNotNull(dto);
        assertNotNull(dto.stats());
        assertEquals(2, dto.stats().size());

        assertEquals("1", dto.stats().get(0).rule_id());
        assertEquals("3", dto.stats().get(0).count());

        assertEquals("2", dto.stats().get(1).rule_id());
        assertEquals("0", dto.stats().get(1).count());

        verify(repo, times(1)).findAllRuleStats();
        verifyNoMoreInteractions(repo);
    }

    @Test
    void getStats_shouldReturnEmptyListWhenNoRules() {
        RuleStatsRepository repo = Mockito.mock(RuleStatsRepository.class);

        when(repo.findAllRuleStats()).thenReturn(List.of());

        RuleStatsService service = new RuleStatsService(repo);

        RuleStatsResponseDto dto = service.getStats();

        assertNotNull(dto);
        assertNotNull(dto.stats());
        assertTrue(dto.stats().isEmpty());

        verify(repo, times(1)).findAllRuleStats();
        verifyNoMoreInteractions(repo);
    }

    /**
     * Стаб для projection из RuleStatsRepository.
     * <p>Урок из 3х часового разбирательства почему падает Spring Data Projection - Никогда не используй Record классы как sub'ы!</p>
     */
    private static final class ProjectionStub implements RuleStatsRepository.RuleStatProjection {

        private final Long ruleId;
        private final Long count;

        private ProjectionStub(Long ruleId, Long count) {
            this.ruleId = ruleId;
            this.count = count;
        }

        @Override
        public Long getRuleId() {
            return ruleId;
        }

        @Override
        public Long getCount() {
            return count;
        }
    }
}