package ru.starbank.recommendation.domain.dto.rule;

/**
 * DTO элемента статистики срабатываний динамического правила.
 *
 * <p>Формат строго соответствует ТЗ Stage 3.</p>
 */
public record RuleStatDto(
        String rule_id,
        String count
) {
}