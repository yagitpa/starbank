package ru.starbank.recommendation.service.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.starbank.recommendation.repository.jdbc.KnowledgeRepository;

/**
 * Сервис управления кешами приложения.
 *
 * <p>Stage 3: после рефактора кеширования на "чистый Caffeine" кеши knowledge DB
 * очищаются напрямую через {@link KnowledgeRepository#clearCaches()}.</p>
 */
@Service
public class CacheManagementService {

    private static final Logger log = LoggerFactory.getLogger(CacheManagementService.class);

    private final KnowledgeRepository knowledgeRepository;

    public CacheManagementService(KnowledgeRepository knowledgeRepository) {
        this.knowledgeRepository = knowledgeRepository;
    }

    /**
     * Полностью очищает все кеши приложения.
     *
     * <p>POST /management/clear-caches</p>
     */
    public void clearAllCaches() {
        knowledgeRepository.clearCaches();
        log.info("Caches cleared: knowledgeRepository caches invalidated");
    }
}