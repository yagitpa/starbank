package ru.starbank.recommendation.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.starbank.recommendation.service.CacheManagementService;

/**
 * Технологические (management) эндпоинты Stage 3.
 */
@RestController
@RequestMapping("/management")
public class CacheManagementController {

    private final CacheManagementService cacheManagementService;

    public CacheManagementController(CacheManagementService cacheManagementService) {
        this.cacheManagementService = cacheManagementService;
    }

    /**
     * Полный сброс всех кешей рекомендаций.
     *
     * <p>POST без тела. После вызова кеш считается очищенным, и следующие запросы
     * должны заново обращаться к БД.</p>
     */
    @PostMapping("/clear-caches")
    @ResponseStatus(HttpStatus.OK)
    public void clearCaches() {
        cacheManagementService.clearAllCaches();
    }
}
