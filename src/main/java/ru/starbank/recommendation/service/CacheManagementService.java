package ru.starbank.recommendation.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.Collection;

/**
 * Сервис управления кешами приложения.
 *
 * <p>Очищает все кеши, зарегистрированные в {@link CacheManager}.</p>
 */
@Service
public class CacheManagementService {

    private static final Logger log = LoggerFactory.getLogger(CacheManagementService.class);

    private final CacheManager cacheManager;

    public CacheManagementService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    /**
     * Полностью очищает все кеши приложения.
     *
     * <p>Используется технологическим эндпоинтом Stage 3: POST /management/clear-caches</p>
     */
    public void clearAllCaches() {
        Collection<String> cacheNames = cacheManager.getCacheNames();
        int cleared = 0;

        for (String cacheName : cacheNames) {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
                cleared++;
            }
        }

        log.info("Caches cleared: totalNames={}, cleared={}", cacheNames.size(), cleared);
    }
}