package ru.starbank.recommendation.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Objects;
import java.util.UUID;

/**
 * Репозиторий для выполнения типовых запросов к "базе знаний" (основной БД).
 *
 * <p>Используется динамическими правилами Stage 2.</p>
 */
@Repository
public class KnowledgeRepository {

    private final JdbcTemplate jdbcTemplate;

    public KnowledgeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate, "jdbcTemplate must not be null");
    }

    /**
     * Проверяет наличие хотя бы одной транзакции пользователя по продукту типа productType.
     */
    public boolean hasAnyTransaction(UUID userId, String productType) {
        String sql = """
            SELECT EXISTS (
                SELECT 1
                FROM transactions t
                JOIN products p ON p.id = t.product_id
                WHERE t.user_id = ?
                  AND p.type = ?
            )
            """;

        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, userId, productType));
    }

    /**
     * Возвращает количество транзакций пользователя по продукту типа productType.
     */
    public long countTransactions(UUID userId, String productType) {
        String sql = """
            SELECT COUNT(*)
            FROM transactions t
            JOIN products p ON p.id = t.product_id
            WHERE t.user_id = ?
              AND p.type = ?
            """;

        Long count = jdbcTemplate.queryForObject(sql, Long.class, userId, productType);
        return count == null ? 0L : count;
    }

    /**
     * Считает сумму amount по пользователю, типу продукта и типу транзакции.
     * Возвращает 0, если транзакций нет.
     */
    public long sumAmount(UUID userId, String productType, String transactionType) {
        String sql = """
            SELECT COALESCE(SUM(t.amount), 0)
            FROM transactions t
            JOIN products p ON p.id = t.product_id
            WHERE t.user_id = ?
              AND p.type = ?
              AND t.type = ?
            """;

        Long sum = jdbcTemplate.queryForObject(sql, Long.class, userId, productType, transactionType);
        return sum == null ? 0L : sum;
    }
}