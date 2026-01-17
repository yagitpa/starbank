package ru.starbank.recommendation.domain.dto.rule;

/**
 * Элемент статистики срабатываний правила.
 *
 * <p>В ТЗ поля называются rule_id и count, и в примере они строками.</p>
 */
public record RuleStatDto(
        String rule_id,
        String count
) {
}