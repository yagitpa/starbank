package ru.starbank.recommendation.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Objects;
import java.util.UUID;

/**
 * Single repository for evaluating recommendation rules using proven working SQL.
 *
 * <p>Note: This repository intentionally uses DB-native units for amount comparisons,
 * matching the verified working example on the same database.</p>
 */
@Repository
public class RecommendationRepository {
    private static final Logger log = LoggerFactory.getLogger(RecommendationRepository.class);

    private final JdbcTemplate jdbcTemplate;

    public RecommendationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate, "jdbcTemplate must not be null");
    }

    public boolean matchesInvest500(UUID userId) {
        String sql = """
            SELECT
                EXISTS (SELECT 1 FROM public.users WHERE id = ?)
                AND EXISTS (
                    SELECT 1
                    FROM transactions t
                    JOIN products p ON t.product_id = p.id
                    WHERE t.user_id = ? AND p.type = 'DEBIT'
                )
                AND NOT EXISTS (
                    SELECT 1
                    FROM transactions t
                    JOIN products p ON t.product_id = p.id
                    WHERE t.user_id = ? AND p.type = 'INVEST'
                )
                AND COALESCE((
                    SELECT SUM(t.amount)
                    FROM transactions t
                    JOIN products p ON t.product_id = p.id
                    WHERE t.user_id = ?
                      AND p.type = 'SAVING'
                      AND t.type = 'DEPOSIT'
                ), 0) > ? AS result
            """;

        return Boolean.TRUE.equals(
                jdbcTemplate.queryForObject(sql, Boolean.class, userId, userId, userId, userId, 1000)
        );
    }

    public boolean matchesTopSaving(UUID userId) {
        String sql = """
            SELECT (
                EXISTS (SELECT 1 FROM public.users u WHERE u.id = ?)
                AND EXISTS (
                    SELECT 1
                    FROM transactions t
                    JOIN products p ON t.product_id = p.id
                    WHERE t.user_id = ? AND p.type = 'DEBIT'
                )
                AND (
                    (SELECT COALESCE(SUM(t.amount), 0)
                       FROM transactions t
                       JOIN products p ON p.id = t.product_id
                      WHERE p.type = 'DEBIT'
                        AND t.type = 'DEPOSIT'
                        AND t.user_id = ?) >= ?
                    OR
                    (SELECT COALESCE(SUM(t.amount), 0)
                       FROM transactions t
                       JOIN products p ON p.id = t.product_id
                      WHERE p.type = 'SAVING'
                        AND t.type = 'DEPOSIT'
                        AND t.user_id = ?) >= ?
                )
                AND (
                    SELECT COALESCE(SUM(CASE WHEN t.type = 'DEPOSIT' THEN t.amount ELSE 0 END), 0)
                         - COALESCE(SUM(CASE WHEN t.type = 'WITHDRAW' THEN t.amount ELSE 0 END), 0)
                    FROM transactions t
                    JOIN products p ON p.id = t.product_id
                    WHERE p.type = 'DEBIT'
                      AND t.user_id = ?
                ) > 0
            ) AS result
            """;

        return Boolean.TRUE.equals(
                jdbcTemplate.queryForObject(
                        sql,
                        Boolean.class,
                        userId,
                        userId,
                        userId, 50_000,
                        userId, 50_000,
                        userId
                )
        );
    }

    public boolean matchesSimpleCredit(UUID userId) {
        String sql = """
            SELECT
                EXISTS (SELECT 1 FROM public.users u WHERE u.id = ?)
                AND NOT EXISTS (
                    SELECT 1
                    FROM transactions t
                    JOIN products p ON p.id = t.product_id
                    WHERE p.type = 'CREDIT'
                      AND t.user_id = ?
                )
                AND 0 < (
                    SELECT COALESCE(SUM(CASE t.type
                        WHEN 'DEPOSIT' THEN t.amount
                        ELSE 0 END), 0)
                         - COALESCE(SUM(CASE t.type
                        WHEN 'WITHDRAW' THEN t.amount
                        ELSE 0 END), 0)
                    FROM transactions t
                    JOIN products p ON p.id = t.product_id
                    WHERE p.type = 'DEBIT'
                      AND t.user_id = ?
                )
                AND ? < (
                    SELECT COALESCE(SUM(t.amount), 0)
                    FROM transactions t
                    JOIN products p ON p.id = t.product_id
                    WHERE p.type = 'DEBIT'
                      AND t.type = 'WITHDRAW'
                      AND t.user_id = ?
                ) AS result
            """;

        return Boolean.TRUE.equals(
                jdbcTemplate.queryForObject(sql, Boolean.class, userId, userId, userId, 100_000, userId)
        );
    }
}