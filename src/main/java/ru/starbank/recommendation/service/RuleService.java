package ru.starbank.recommendation.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.starbank.recommendation.domain.dto.rule.CreateRuleRequestDto;
import ru.starbank.recommendation.domain.dto.rule.RuleDto;
import ru.starbank.recommendation.domain.dto.rule.RuleListResponseDto;
import ru.starbank.recommendation.domain.dto.rule.RuleQueryDto;
import ru.starbank.recommendation.domain.rules.entity.RuleEntity;
import ru.starbank.recommendation.domain.rules.entity.RuleQueryEntity;
import ru.starbank.recommendation.exception.RuleNotFoundException;
import ru.starbank.recommendation.repository.RuleRepository;
import ru.starbank.recommendation.domain.rules.entity.RuleStatsEntity;
import ru.starbank.recommendation.repository.RuleStatsRepository;

import java.util.List;

/**
 * Сервис управления динамическими правилами (rules DB).
 *
 * <p>Важно: используется отдельный TransactionManager для rules DB
 * (см. RulesDataSourceConfig: rulesTransactionManager).</p>
 */
@Service
public class RuleService {
    private static final Logger log = LoggerFactory.getLogger(RuleService.class);
    private static final TypeReference<List<String>> STRING_LIST_TYPE = new TypeReference<>() {};

    private final RuleRepository ruleRepository;
    private final ObjectMapper objectMapper;
    private final RuleStatsRepository ruleStatsRepository;

    public RuleService(RuleRepository ruleRepository, ObjectMapper objectMapper, RuleStatsRepository ruleStatsRepository) {
        this.ruleRepository = ruleRepository;
        this.objectMapper = objectMapper;
        this.ruleStatsRepository = ruleStatsRepository;
    }

    /**
     * Создаёт динамическое правило и возвращает DTO сохранённого правила.
     */
    @Transactional(transactionManager = "rulesTransactionManager")
    public RuleDto createRule(CreateRuleRequestDto request) {
        log.info("Creating dynamic rule: product_id={}, product_name={}, queries={}",
                request.productId(), request.productName(), request.rule().size());

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
        log.info("Dynamic rule created: id={}, product_id={}", saved.getId(), saved.getProductId());
        ruleStatsRepository.save(new RuleStatsEntity(saved));
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

        log.debug("Loaded dynamic rules: count={}", data.size());
        return new RuleListResponseDto(data);
    }

    /**
     * Удаляет правило по id.
     * Если правила нет — кидает исключение (позже обработаем в ControllerAdvice).
     */
    @Transactional(transactionManager = "rulesTransactionManager")
    public void deleteRule(long id) {
        log.info("Deleting dynamic rule: id={}", id);

        if (!ruleRepository.existsById(id)) {
            throw new RuleNotFoundException(id);
        }

        ruleRepository.deleteById(id);
        log.info("Dynamic rule deleted: id={}", id);
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