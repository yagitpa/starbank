package ru.starbank.recommendation.service;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.starbank.recommendation.domain.dto.RecommendationDto;
import ru.starbank.recommendation.domain.dto.RecommendationResponseDto;
import ru.starbank.recommendation.domain.rules.RecommendationRuleSet;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class RecommendationServiceTest {

    @Test
    void shouldReturnRecommendations_fromFixedAndDynamic() {
        // given
        RecommendationRuleSet ruleSet = Mockito.mock(RecommendationRuleSet.class);
        DynamicRulesRecommendationService dynamicService = Mockito.mock(DynamicRulesRecommendationService.class);

        RecommendationService service = new RecommendationService(List.of(ruleSet), dynamicService);

        UUID userId = UUID.randomUUID();

        // fixed rules -> ничего не нашли
        when(ruleSet.check(userId)).thenReturn(Optional.empty());

        // dynamic rules -> тоже пусто
        when(dynamicService.getDynamicRecommendations(userId)).thenReturn(List.of());

        // when
        RecommendationResponseDto response = service.getRecommendations(userId);

        // then
        assertNotNull(response);

        // если у вас record:
        assertEquals(userId, response.userId());
        assertNotNull(response.recommendations());
        assertEquals(0, response.recommendations().size());
    }

    @Test
    void shouldIncludeFixedRecommendation_whenRuleSetMatches() {
        // given
        RecommendationRuleSet ruleSet = Mockito.mock(RecommendationRuleSet.class);
        DynamicRulesRecommendationService dynamicService = Mockito.mock(DynamicRulesRecommendationService.class);

        RecommendationService service = new RecommendationService(List.of(ruleSet), dynamicService);

        UUID userId = UUID.randomUUID();
        RecommendationDto fixed = new RecommendationDto(
                UUID.randomUUID(),
                "Fixed product",
                "Fixed text"
        );

        when(ruleSet.check(userId)).thenReturn(Optional.of(fixed));
        when(dynamicService.getDynamicRecommendations(userId)).thenReturn(List.of());

        // when
        RecommendationResponseDto response = service.getRecommendations(userId);

        // then
        assertEquals(1, response.recommendations().size());
        assertEquals(fixed, response.recommendations().get(0));
    }
}