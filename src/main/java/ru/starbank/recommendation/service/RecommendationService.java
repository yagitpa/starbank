package ru.starbank.recommendation.service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ru.starbank.recommendation.domain.dto.RecommendationDto;
import ru.starbank.recommendation.domain.dto.RecommendationResponseDto;
import ru.starbank.recommendation.domain.rules.RecommendationRuleSet;

/**
 * Service that aggregates product recommendations for a user.
 */
@Service
public class RecommendationService {
    private static final Logger log = LoggerFactory.getLogger(RecommendationService.class);


    private final List<RecommendationRuleSet> ruleSets;

    /**
     * Constructor injection (DIP).
     *
     * @param ruleSets all available recommendation rule sets
     */
    public RecommendationService(List<RecommendationRuleSet> ruleSets) {
        this.ruleSets = Objects.requireNonNull(ruleSets, "ruleSets must not be null");
    }

    /**
     * Builds recommendations list for a given user.
     *
     * @param userId user id
     * @return response DTO with possibly empty recommendations list
     */
    public RecommendationResponseDto getRecommendations(UUID userId) {
        log.debug("Building recommendations for user_id={}, ruleSets={}", userId, ruleSets.size());

        List<RecommendationDto> recommendations = ruleSets.stream()
                                                          .flatMap(ruleSet -> ruleSet.check(userId).stream())
                                                          .toList();

        log.debug("Built recommendations for user_id={}, count={}", userId, recommendations.size());

        return new RecommendationResponseDto(userId, recommendations);
    }
}
