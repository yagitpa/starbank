package ru.starbank.recommendation.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.UUID;

/**
 * API response DTO for recommendations.
 *
 * @param userId          requested user id (serialized as "user_id")
 * @param recommendations list of matched recommendations
 */
public record RecommendationResponseDto(
        @JsonProperty("user_id") UUID userId,
        @JsonProperty("recommendations") List<RecommendationDto> recommendations
) {
}
