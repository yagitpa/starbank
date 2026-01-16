package ru.starbank.recommendation.config;

import java.time.Duration;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Конфигурация кешей (Caffeine) для Stage 2.
 *
 * <p>По чеклисту используем не более 3 кешей для SQL-запросов к knowledge DB.</p>
 */
@Configuration
@EnableCaching
public class CacheConfig {

    public static final String CACHE_HAS_ANY_TRANSACTION = "knowledgeHasAnyTransaction";
    public static final String CACHE_COUNT_TRANSACTIONS = "knowledgeCountTransactions";
    public static final String CACHE_SUM_AMOUNT = "knowledgeSumAmount";

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager manager = new SimpleCacheManager();

        Caffeine<Object, Object> baseSpec = Caffeine.newBuilder()
                                                    .maximumSize(50_000)
                                                    .expireAfterWrite(Duration.ofMinutes(10));

        manager.setCaches(List.of(
                new CaffeineCache(CACHE_HAS_ANY_TRANSACTION, baseSpec.build()),
                new CaffeineCache(CACHE_COUNT_TRANSACTIONS, baseSpec.build()),
                new CaffeineCache(CACHE_SUM_AMOUNT, baseSpec.build())
        ));

        return manager;
    }
}