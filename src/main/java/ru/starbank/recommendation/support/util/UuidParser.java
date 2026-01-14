package ru.starbank.recommendation.support.util;

import java.util.UUID;

import ru.starbank.recommendation.support.exception.InvalidUserIdException;

/**
 * Utility for parsing UUID from API input.
 */
public final class UuidParser {

    private UuidParser() {
    }

    /**
     * Parses UUID from string or throws a domain-specific exception.
     *
     * @param raw raw value
     * @return parsed UUID
     */
    public static UUID parseUserId(String raw) {
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException ex) {
            throw new InvalidUserIdException("Invalid user_id. Expected UUID format.", ex);
        }
    }
}
