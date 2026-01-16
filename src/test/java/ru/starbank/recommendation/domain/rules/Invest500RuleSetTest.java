package ru.starbank.recommendation.domain.rules;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.starbank.recommendation.repository.RecommendationRepository;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class Invest500RuleSetTest {

    @Test
    void shouldReturnRecommendationWhenMatches() {
        RecommendationRepository repo = Mockito.mock(RecommendationRepository.class);
        Invest500RuleSet ruleSet = new Invest500RuleSet(repo);

        UUID userId = UUID.randomUUID();
        when(repo.matchesInvest500(userId)).thenReturn(true);

        assertThat(ruleSet.check(userId)).isPresent();
        assertThat(ruleSet.check(userId).get().name()).isEqualTo("Invest 500");
    }

    @Test
    void shouldReturnEmptyWhenNotMatches() {
        RecommendationRepository repo = Mockito.mock(RecommendationRepository.class);
        Invest500RuleSet ruleSet = new Invest500RuleSet(repo);

        UUID userId = UUID.randomUUID();
        when(repo.matchesInvest500(userId)).thenReturn(false);

        assertThat(ruleSet.check(userId)).isEmpty();
    }
}