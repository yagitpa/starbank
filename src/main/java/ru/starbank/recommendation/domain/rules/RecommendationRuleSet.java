package ru.starbank.recommendation.domain.rules;

import ru.starbank.recommendation.domain.dto.RecommendationDto;

import java.util.Optional;
import java.util.UUID;

/**
 * Contract for a single product recommendation rule set.
 *
 * <p>Implementation must return Optional recommendation when user matches conditions.</p>
 */
public interface RecommendationRuleSet {

    /**
     * Checks whether user matches rule set.
     *
     * @param userId user id
     * @return optional recommendation
     */
    Optional<RecommendationDto> check(UUID userId);
}
