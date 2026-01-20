package ru.starbank.recommendation.config.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.UUID;

/**
 * Конфигурация кешей для репозитория knowledge DB (Stage 3).
 *
 * <p>Кешируем результаты SQL-запросов к knowledge DB (read-only), используя явный Caffeine cache.get(key, mappingFn).</p>
 *
 * <p>По требованиям Stage 3: не более 3 кешей.</p>
 *
 * <p>@SuppressWarnings (подавление предупреждений) добавлен исключительно из-за предупреждений IDEA: Non-null type argument is expected.
 * В record-классах мы гарантируем что переданные параметры не будут Null</p>
 *
 */
@Configuration
@SuppressWarnings({"nullness", "NullableProblems"})

public class CacheConfig {

    @Bean
    public Cache<HasAnyTransactionKey, Boolean> hasAnyTransactionCache() {
        return Caffeine.newBuilder()
                       .maximumSize(50_000)
                       .expireAfterWrite(Duration.ofMinutes(15))
                       .build();
    }

    @Bean
    public Cache<CountTransactionsKey, Integer> countTransactionsCache() {
        return Caffeine.newBuilder()
                       .maximumSize(50_000)
                       .expireAfterWrite(Duration.ofMinutes(15))
                       .build();
    }

    @Bean
    public Cache<SumAmountKey, Long> sumAmountCache() {
        return Caffeine.newBuilder()
                       .maximumSize(100_000)
                       .expireAfterWrite(Duration.ofMinutes(15))
                       .build();
    }

    /**
     * Ключ кеша: есть ли у пользователя хотя бы одна транзакция по продуктам типа productType.
     */
    public record HasAnyTransactionKey(UUID userId, String productType) {}

    /**
     * Ключ кеша: количество транзакций пользователя по продуктам типа productType.
     */
    public record CountTransactionsKey(UUID userId, String productType) {}

    /**
     * Ключ кеша: сумма amount по пользователю, productType и transactionType.
     */
    public record SumAmountKey(UUID userId, String productType, String transactionType) {}
}