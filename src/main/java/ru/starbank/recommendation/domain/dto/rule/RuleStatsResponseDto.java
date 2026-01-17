package ru.starbank.recommendation.domain.dto.rule;

import java.util.List;

/**
 * Ответ эндпоинта GET /rule/stats.
 *
 * <pre>
 * {
 *   "stats": [
 *     { "rule_id": "1", "count": "3" }
 *   ]
 * }
 * </pre>
 */
public record RuleStatsResponseDto(
        List<RuleStatDto> stats
) {
}