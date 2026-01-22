package ru.starbank.recommendation.domain.dto.rule;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO элемента статистики срабатываний динамического правила.
 *
 * <p>Формат строго соответствует ТЗ Stage 3.</p>
 */
public record RuleStatsDto(
        @Schema(description = "ID правила", example = "1")
        String rule_id,

        @Schema(description = "Количество срабатываний правила", example = "10")
        String count
) {
}