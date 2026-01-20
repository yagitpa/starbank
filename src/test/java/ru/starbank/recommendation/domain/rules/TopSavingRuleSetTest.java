package ru.starbank.recommendation.domain.rules;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.starbank.recommendation.repository.jdbc.RecommendationRepository;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class TopSavingRuleSetTest {

    @Test
    void shouldReturnRecommendationWhenMatches() {
        RecommendationRepository repo = Mockito.mock(RecommendationRepository.class);
        TopSavingRuleSet ruleSet = new TopSavingRuleSet(repo);

        UUID userId = UUID.randomUUID();
        when(repo.matchesTopSaving(userId)).thenReturn(true);

        assertThat(ruleSet.check(userId)).isPresent();
        assertThat(ruleSet.check(userId).get().name()).isEqualTo("Top Saving");
    }

    @Test
    void shouldReturnEmptyWhenNotMatches() {
        RecommendationRepository repo = Mockito.mock(RecommendationRepository.class);
        TopSavingRuleSet ruleSet = new TopSavingRuleSet(repo);

        UUID userId = UUID.randomUUID();
        when(repo.matchesTopSaving(userId)).thenReturn(false);

        assertThat(ruleSet.check(userId)).isEmpty();
    }
}