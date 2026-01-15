package ru.starbank.recommendation.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.starbank.recommendation.domain.dto.rule.CreateRuleRequestDto;
import ru.starbank.recommendation.domain.dto.rule.RuleDto;
import ru.starbank.recommendation.domain.dto.rule.RuleListResponseDto;
import ru.starbank.recommendation.domain.dto.rule.RuleQueryDto;
import ru.starbank.recommendation.domain.rules.entity.RuleEntity;
import ru.starbank.recommendation.domain.rules.entity.RuleQueryEntity;
import ru.starbank.recommendation.repository.RuleRepository;

import java.util.List;

/**
 * Сервис управления динамическими правилами (rules DB).
 *
 * <p>Важно: используется отдельный TransactionManager для rules DB
 * (см. RulesDataSourceConfig: rulesTransactionManager).</p>
 */
@Service
public class RuleService {

    private static final TypeReference<List<String>> STRING_LIST_TYPE = new TypeReference<>() {};

    private final RuleRepository ruleRepository;
    private final ObjectMapper objectMapper;

    public RuleService(RuleRepository ruleRepository, ObjectMapper objectMapper) {
        this.ruleRepository = ruleRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Создаёт динамическое правило и возвращает DTO сохранённого правила.
     */
    @Transactional(transactionManager = "rulesTransactionManager")
    public RuleDto createRule(CreateRuleRequestDto request) {
        RuleEntity entity = new RuleEntity(
                request.productName(),
                request.productId(),
                request.productText()
        );

        // rule[] из DTO -> queries[] в entity
        for (RuleQueryDto q : request.rule()) {
            RuleQueryEntity queryEntity = new RuleQueryEntity(
                    q.query(),
                    serializeArguments(q.arguments()),
                    Boolean.TRUE.equals(q.negate())
            );
            entity.addQuery(queryEntity);
        }

        RuleEntity saved = ruleRepository.save(entity);
        return toDto(saved);
    }

    /**
     * Возвращает список всех динамических правил.
     * Формат соответствует контракту GET /rule: { "data": [...] }.
     */
    @Transactional(readOnly = true, transactionManager = "rulesTransactionManager")
    public RuleListResponseDto getRules() {
        List<RuleDto> data = ruleRepository.findAll()
                                           .stream()
                                           .map(this::toDto)
                                           .toList();

        return new RuleListResponseDto(data);
    }

    /**
     * Удаляет правило по id.
     * Если правила нет — кидает исключение (позже обработаем в ControllerAdvice).
     */
    @Transactional(transactionManager = "rulesTransactionManager")
    public void deleteRule(long id) {
        if (!ruleRepository.existsById(id)) {
            throw new IllegalArgumentException("Правило с id=" + id + " не найдено");
        }
        ruleRepository.deleteById(id);
    }

    // -------------------- Mapping --------------------

    private RuleDto toDto(RuleEntity entity) {
        List<RuleQueryDto> rule = entity.getQueries()
                                        .stream()
                                        .map(this::toDto)
                                        .toList();

        return new RuleDto(
                entity.getId(),
                entity.getProductName(),
                entity.getProductId(),
                entity.getProductText(),
                rule
        );
    }

    private RuleQueryDto toDto(RuleQueryEntity entity) {
        return new RuleQueryDto(
                entity.getQuery(),
                deserializeArguments(entity.getArguments()),
                entity.isNegate()
        );
    }

    // -------------------- JSON helpers --------------------

    /**
     * Сериализует список аргументов в JSON-массив строк для хранения в колонке TEXT.
     */
    private String serializeArguments(List<String> arguments) {
        try {
            return objectMapper.writeValueAsString(arguments);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Не удалось сериализовать arguments в JSON", e);
        }
    }

    /**
     * Десериализует JSON-массив строк из БД обратно в List<String>.
     */
    private List<String> deserializeArguments(String argumentsJson) {
        try {
            return objectMapper.readValue(argumentsJson, STRING_LIST_TYPE);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Не удалось распарсить arguments из JSON: " + argumentsJson, e);
        }
    }
}