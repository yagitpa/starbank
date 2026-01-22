package ru.starbank.recommendation.repository.jdbc;

import com.github.benmanes.caffeine.cache.Cache;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.starbank.recommendation.config.cache.CacheConfig;

import java.util.Objects;
import java.util.UUID;

/**
 * Репозиторий knowledge DB (read-only).
 *
 * <p>Stage 3: кеширование через явный Caffeine get-if-absent-compute.</p>
 */
@Repository
@SuppressWarnings({"nullness", "NullableProblems"})
public class KnowledgeRepository {

    private final JdbcTemplate jdbcTemplate;

    private final Cache<CacheConfig.HasAnyTransactionKey, Boolean> hasAnyTransactionCache;
    private final Cache<CacheConfig.CountTransactionsKey, Integer> countTransactionsCache;
    private final Cache<CacheConfig.SumAmountKey, Long> sumAmountCache;

    public KnowledgeRepository(
            JdbcTemplate jdbcTemplate,
            Cache<CacheConfig.HasAnyTransactionKey, Boolean> hasAnyTransactionCache,
            Cache<CacheConfig.CountTransactionsKey, Integer> countTransactionsCache,
            Cache<CacheConfig.SumAmountKey, Long> sumAmountCache
    ) {
        this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate, "jdbcTemplate must not be null");
        this.hasAnyTransactionCache = Objects.requireNonNull(hasAnyTransactionCache, "hasAnyTransactionCache must not be null");
        this.countTransactionsCache = Objects.requireNonNull(countTransactionsCache, "countTransactionsCache must not be null");
        this.sumAmountCache = Objects.requireNonNull(sumAmountCache, "sumAmountCache must not be null");
    }

    /**
     * Проверяет, является ли пользователь пользователем продукта типа productType:
     * есть ли хотя бы одна транзакция по продуктам данного типа.
     */
    public boolean hasAnyTransaction(UUID userId, String productType) {
        CacheConfig.HasAnyTransactionKey key = new CacheConfig.HasAnyTransactionKey(userId, productType);

        return Boolean.TRUE.equals(hasAnyTransactionCache.get(key, k -> {
            Integer cnt = jdbcTemplate.queryForObject("""
                    SELECT COUNT(*)
                    FROM transactions t
                    JOIN products p ON p.id = t.product_id
                    WHERE t.user_id = ?
                      AND p.type = ?
                    """, Integer.class, k.userId(), k.productType());

            return cnt != null && cnt > 0;
        }));
    }

    /**
     * Возвращает количество транзакций пользователя по продуктам типа productType.
     * Используется для ACTIVE_USER_OF (threshold >= 5).
     */
    public int countTransactions(UUID userId, String productType) {
        CacheConfig.CountTransactionsKey key = new CacheConfig.CountTransactionsKey(userId, productType);

        return countTransactionsCache.get(key, k -> {
            Integer cnt = jdbcTemplate.queryForObject("""
                    SELECT COUNT(*)
                    FROM transactions t
                    JOIN products p ON p.id = t.product_id
                    WHERE t.user_id = ?
                      AND p.type = ?
                    """, Integer.class, k.userId(), k.productType());

            return cnt == null ? 0 : cnt;
        });
    }

    /**
     * Возвращает сумму amount по транзакциям типа transactionType и продуктам типа productType для пользователя.
     *
     * <p>Например: productType=DEBIT, transactionType=WITHDRAW.</p>
     */
    public long sumAmount(UUID userId, String productType, String transactionType) {
        CacheConfig.SumAmountKey key = new CacheConfig.SumAmountKey(userId, productType, transactionType);

        return sumAmountCache.get(key, k -> {
            Long sum = jdbcTemplate.queryForObject("""
                    SELECT COALESCE(SUM(t.amount), 0)
                    FROM transactions t
                    JOIN products p ON p.id = t.product_id
                    WHERE t.user_id = ?
                      AND p.type = ?
                      AND t.type = ?
                    """, Long.class, k.userId(), k.productType(), k.transactionType());

            return sum == null ? 0L : sum;
        });
    }

    /**
     * Полная очистка всех кешей knowledge DB.
     * Используется management endpoint /management/clear-caches.
     */
    public void clearCaches() {
        hasAnyTransactionCache.invalidateAll();
        countTransactionsCache.invalidateAll();
        sumAmountCache.invalidateAll();
    }
}