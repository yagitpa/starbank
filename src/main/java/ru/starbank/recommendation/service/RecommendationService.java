package ru.starbank.recommendation.service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

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
    private final DynamicRulesRecommendationService dynamicRulesRecommendationService;

    /**
     * Constructor injection (DIP).
     *
     * @param ruleSets all available recommendation rule sets (fixed Stage 1 rules)
     * @param dynamicRulesRecommendationService dynamic Stage 2 rules provider
     */
    public RecommendationService(
            List<RecommendationRuleSet> ruleSets,
            DynamicRulesRecommendationService dynamicRulesRecommendationService
    ) {
        this.ruleSets = Objects.requireNonNull(ruleSets, "ruleSets must not be null");
        this.dynamicRulesRecommendationService = Objects.requireNonNull(
                dynamicRulesRecommendationService,
                "dynamicRulesRecommendationService must not be null"
        );
    }

    /**
     * Builds recommendations list for a given user.
     *
     * @param userId user id
     * @return response DTO with possibly empty recommendations list
     */
    public RecommendationResponseDto getRecommendations(UUID userId) {
        log.debug("Building recommendations for user_id={}, ruleSets={}", userId, ruleSets.size());

        List<RecommendationDto> fixedRecommendations = ruleSets.stream()
                                                               .flatMap(ruleSet -> ruleSet.check(userId).stream())
                                                               .toList();

        List<RecommendationDto> dynamicRecommendations = dynamicRulesRecommendationService.getDynamicRecommendations(userId);

        List<RecommendationDto> recommendations = Stream.concat(
                                                                fixedRecommendations.stream(),
                                                                dynamicRecommendations.stream()
                                                        )
                                                        .toList();

        log.debug("Built recommendations for user_id={}, fixed={}, dynamic={}, total={}",
                userId, fixedRecommendations.size(), dynamicRecommendations.size(), recommendations.size());

        return new RecommendationResponseDto(userId, recommendations);
    }
}