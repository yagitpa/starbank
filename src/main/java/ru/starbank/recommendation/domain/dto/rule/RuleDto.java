package ru.starbank.recommendation.domain.dto.rule;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * DTO динамического правила (используется в ответах POST /rule и GET /rule).
 */
public record RuleDto(

        @NotNull
        @JsonProperty("id")
        Long id,

        @NotBlank
        @JsonProperty("product_name")
        String productName,

        @NotBlank
        @JsonProperty("product_id")
        String productId,

        @NotBlank
        @JsonProperty("product_text")
        String productText,

        @NotNull
        @Size(min = 1)
        @Valid
        @JsonProperty("rule")
        List<RuleQueryDto> rule
) {
}