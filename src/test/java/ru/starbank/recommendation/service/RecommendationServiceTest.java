package ru.starbank.recommendation.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import ru.starbank.recommendation.domain.dto.RecommendationDto;
import ru.starbank.recommendation.domain.rules.RecommendationRuleSet;

class RecommendationServiceTest {

    @Test
    void shouldCollectOnlyPresentRecommendations() {
        UUID userId = UUID.randomUUID();

        RecommendationRuleSet r1 = u -> Optional.of(new RecommendationDto(UUID.randomUUID(), "A", "text"));
        RecommendationRuleSet r2 = u -> Optional.empty();
        RecommendationRuleSet r3 = u -> Optional.of(new RecommendationDto(UUID.randomUUID(), "B", "text"));

        RecommendationService service = new RecommendationService(List.of(r1, r2, r3));

        var response = service.getRecommendations(userId);

        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.recommendations()).hasSize(2);
        assertThat(response.recommendations().get(0).name()).isEqualTo("A");
        assertThat(response.recommendations().get(1).name()).isEqualTo("B");
    }
}
