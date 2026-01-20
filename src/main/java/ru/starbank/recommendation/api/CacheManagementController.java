package ru.starbank.recommendation.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.starbank.recommendation.service.CacheManagementService;

/**
 * Технологические (management) эндпоинты Stage 3.
 */
@Tag(name = "3. Cache management", description = "Управление кэшами")
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
    @Operation(summary = "Очистить все кэши",
            description = "Очистить все кэшированные данные в системе.",
            responses = {
                    @ApiResponse(responseCode = "204",
                            description = "Кэши успешно очищены")
            })
    @PostMapping("/clear-caches")
    @ResponseStatus(HttpStatus.OK)
    public void clearCaches() {
        cacheManagementService.clearAllCaches();
    }
}
