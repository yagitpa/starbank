package ru.starbank.recommendation.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

/**
 * API response DTO for recommendations.
 *
 * @param userId          requested user id (serialized as "user_id")
 * @param recommendations list of matched recommendations
 */
public record RecommendationResponseDto(
        @Schema(description = "ID пользователя", example = "d4a4d619-9a0c-4fc5-b0cb-76c49409546b")
        @JsonProperty("user_id") UUID userId,

        @Schema(description = "Список рекомендаций")
        @JsonProperty("recommendations") List<RecommendationDto> recommendations
) {
}
