package ru.starbank.recommendation.domain.dto.rule;

import java.util.List;

/**
 * Ответ эндпоинта GET /rule/stats.
 */
public record RuleStatsResponseDto(
        List<RuleStatDto> stats
) {
}