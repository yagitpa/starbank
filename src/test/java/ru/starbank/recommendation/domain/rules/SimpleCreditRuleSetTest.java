package ru.starbank.recommendation.domain.rules;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.starbank.recommendation.repository.RecommendationRepository;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class SimpleCreditRuleSetTest {

    @Test
    void shouldReturnRecommendationWhenMatches() {
        RecommendationRepository repo = Mockito.mock(RecommendationRepository.class);
        SimpleCreditRuleSet ruleSet = new SimpleCreditRuleSet(repo);

        UUID userId = UUID.randomUUID();
        when(repo.matchesSimpleCredit(userId)).thenReturn(true);

        assertThat(ruleSet.check(userId)).isPresent();
        assertThat(ruleSet.check(userId).get().name()).isEqualTo("Простой кредит");
    }

    @Test
    void shouldReturnEmptyWhenNotMatches() {
        RecommendationRepository repo = Mockito.mock(RecommendationRepository.class);
        SimpleCreditRuleSet ruleSet = new SimpleCreditRuleSet(repo);

        UUID userId = UUID.randomUUID();
        when(repo.matchesSimpleCredit(userId)).thenReturn(false);

        assertThat(ruleSet.check(userId)).isEmpty();
    }
}