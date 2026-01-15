package ru.starbank.recommendation.repository;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.starbank.recommendation.config.CacheConfig;

import java.util.Objects;
import java.util.UUID;

/**
 * Репозиторий для выполнения типовых запросов к "базе знаний" (основной БД).
 *
 * <p>Используется динамическими правилами Stage 2.</p>
 * <p>Stage 2: результаты запросов кешируются через Caffeine.</p>
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
    @Cacheable(
            cacheNames = CacheConfig.CACHE_HAS_ANY_TRANSACTION,
            key = "{#userId, #productType}"
    )
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
    @Cacheable(
            cacheNames = CacheConfig.CACHE_COUNT_TRANSACTIONS,
            key = "{#userId, #productType}"
    )
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
    @Cacheable(
            cacheNames = CacheConfig.CACHE_SUM_AMOUNT,
            key = "{#userId, #productType, #transactionType}"
    )
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