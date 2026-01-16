package ru.starbank.recommendation.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.starbank.recommendation.domain.dto.RecommendationDto;
import ru.starbank.recommendation.domain.rules.engine.QueryEngine;
import ru.starbank.recommendation.domain.rules.entity.RuleEntity;
import ru.starbank.recommendation.exception.InvalidProductIdException;
import ru.starbank.recommendation.repository.RuleRepository;

/**
 * Сервис, который вычисляет рекомендации из динамических правил (rules DB).
 */
@Service
public class DynamicRulesRecommendationService {
    private static final Logger log = LoggerFactory.getLogger(DynamicRulesRecommendationService.class);

    private final RuleRepository ruleRepository;
    private final QueryEngine queryEngine;

    public DynamicRulesRecommendationService(RuleRepository ruleRepository, QueryEngine queryEngine) {
        this.ruleRepository = ruleRepository;
        this.queryEngine = queryEngine;
    }

    /**
     * Возвращает список рекомендаций, полученных из динамических правил.
     *
     * <p>AND-логика: правило считается выполненным, если ВСЕ его queries истинны.</p>
     */
    @Transactional(readOnly = true, transactionManager = "rulesTransactionManager")
    public List<RecommendationDto> getDynamicRecommendations(UUID userId) {
        List<RuleEntity> rules = ruleRepository.findAllWithQueries();
        if (rules.isEmpty()) {
            log.debug("No dynamic rules found. user_id={}", userId);
            return List.of();
        }

        int matchedCount = 0;
        List<RecommendationDto> result = new ArrayList<>();

        for (RuleEntity rule : rules) {
            boolean matched = rule.getQueries().stream()
                                  .allMatch(q -> queryEngine.evaluate(userId, q));

            if (matched) {
                matchedCount++;
                UUID productUuid = parseProductId(rule.getProductId());
                result.add(new RecommendationDto(productUuid, rule.getProductName(), rule.getProductText()));
            }
        }

        log.info("Dynamic rules evaluated: user_id={}, rules={}, matched={}, recommendations={}",
                userId, rules.size(), matchedCount, result.size());
        return result;
    }

    private UUID parseProductId(String productId) {
        try {
            return UUID.fromString(productId);
        } catch (Exception e) {
            throw new InvalidProductIdException(productId, e);
        }
    }
}