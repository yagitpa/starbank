package ru.starbank.recommendation.domain.dto;

import java.util.UUID;

/**
 * DTO representing a single recommended product.
 *
 * @param id product id
 * @param name product name
 * @param text recommendation description
 */
public record RecommendationDto(UUID id, String name, String text) {
}
