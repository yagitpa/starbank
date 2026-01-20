package ru.starbank.recommendation.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.starbank.recommendation.domain.dto.rule.CreateRuleRequestDto;
import ru.starbank.recommendation.domain.dto.rule.RuleDto;
import ru.starbank.recommendation.domain.dto.rule.RuleListResponseDto;
import ru.starbank.recommendation.domain.dto.rule.RuleStatsDto;
import ru.starbank.recommendation.domain.dto.rule.RuleStatsResponseDto;
import ru.starbank.recommendation.service.dynamic.RuleService;
import ru.starbank.recommendation.service.dynamic.RuleStatsService;

/**
 * REST API управления динамическими правилами рекомендаций.
 *
 * <p>Контроллер "тонкий": только HTTP-слой (приём/возврат данных).
 * Вся бизнес-логика находится в {@link RuleService}.</p>
 */
@Tag(name = "Rules", description = "Управление динамическими правилами рекомендаций")
@RestController
@RequestMapping("/rule")
public class RuleController {

    private final RuleService ruleService;
    private final RuleStatsService ruleStatsService;

    public RuleController(RuleService ruleService, RuleStatsService ruleStatsService) {
        this.ruleService = ruleService;
        this.ruleStatsService = ruleStatsService;
    }

    /**
     * Создаёт новое динамическое правило.
     *
     * @param request тело запроса согласно Stage 2 ТЗ
     * @return сохранённое правило с id
     */
    @Operation(summary = "Создать новое правило",
            description = "Создаёт новое правило в системе.",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Правило успешно создано",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = RuleDto.class))),
                    @ApiResponse(responseCode = "400", description = "Неверные данные запроса")
            })
    @PostMapping
    public RuleDto createRule(@Valid @RequestBody CreateRuleRequestDto request) {
        return ruleService.createRule(request);
    }

    /**
     * Возвращает список всех динамических правил.
     *
     * @return { "data": [ ... ] }
     */
    @Operation(summary = "Получить все правила",
            description = "Возвращает список всех правил.",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Успешный запрос",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = RuleDto.class)))
            })
    @GetMapping
    public RuleListResponseDto getRules() {
        return ruleService.getRules();
    }

    /**
     * Удаляет правило по id.
     *
     * @param id идентификатор правила
     */
    @Operation(summary = "Удалить правило",
            description = "Удаляет правило по указанному ID.",
            responses = {
                    @ApiResponse(responseCode = "204",
                            description = "Правило успешно удалено"),
                    @ApiResponse(responseCode = "404", description = "Правило не найдено")
            })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRule(@PathVariable("id") long id) {
        ruleService.deleteRule(id);
    }

    /**
     * Возвращает статистику срабатываний по всем динамическим правилам.
     *
     * @return { "stats": [ ... ] }
     */
    @Operation(summary = "Получить статистику по всем правилам",
            description = "Возвращает статистику по всем правилам, включая их количество и срабатывания.",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Успешный запрос",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = RuleStatsDto.class))),
                    @ApiResponse(responseCode = "404", description = "Правила не найдены")
            })
    @GetMapping("/stats")
    public RuleStatsResponseDto getRuleStats() {
        return ruleStatsService.getStats();
    }
}