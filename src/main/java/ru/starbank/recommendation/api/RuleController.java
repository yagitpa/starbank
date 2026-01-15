package ru.starbank.recommendation.api;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import ru.starbank.recommendation.domain.dto.rule.CreateRuleRequestDto;
import ru.starbank.recommendation.domain.dto.rule.RuleDto;
import ru.starbank.recommendation.domain.dto.rule.RuleListResponseDto;
import ru.starbank.recommendation.service.RuleService;

/**
 * REST API управления динамическими правилами рекомендаций.
 *
 * <p>Контроллер "тонкий": только HTTP-слой (приём/возврат данных).
 * Вся бизнес-логика находится в {@link RuleService}.</p>
 */
@RestController
@RequestMapping("/rule")
public class RuleController {

    private final RuleService ruleService;

    public RuleController(RuleService ruleService) {
        this.ruleService = ruleService;
    }

    /**
     * Создаёт новое динамическое правило.
     *
     * @param request тело запроса согласно Stage 2 ТЗ
     * @return сохранённое правило с id
     */
    @PostMapping
    public RuleDto createRule(@Valid @RequestBody CreateRuleRequestDto request) {
        return ruleService.createRule(request);
    }

    /**
     * Возвращает список всех динамических правил.
     *
     * @return { "data": [ ... ] }
     */
    @GetMapping
    public RuleListResponseDto getRules() {
        return ruleService.getRules();
    }

    /**
     * Удаляет правило по id.
     *
     * @param id идентификатор правила
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRule(@PathVariable("id") long id) {
        ruleService.deleteRule(id);
    }
}