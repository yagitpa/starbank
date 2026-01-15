package ru.starbank.recommendation.domain.dto.rule;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * DTO ответа для GET /rule:
 * {
 *   "data": [ ... ]
 * }
 */
public record RuleListResponseDto(
        @NotNull
        @JsonProperty("data")
        List<RuleDto> data
) {
}