package ru.starbank.recommendation.domain.dto.management;

/**
 * DTO ответа для GET /management/info.
 */
public record ManagementInfoDto(
        String name,
        String version
) {
}