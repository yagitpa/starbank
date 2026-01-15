package ru.starbank.recommendation.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.starbank.recommendation.domain.dto.RecommendationDto;
import ru.starbank.recommendation.domain.rules.engine.QueryEngine;
import ru.starbank.recommendation.domain.rules.entity.RuleEntity;
import ru.starbank.recommendation.repository.RuleRepository;

/**
 * Сервис, который вычисляет рекомендации из динамических правил (rules DB).
 */
@Service
public class DynamicRulesRecommendationService {

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
            return List.of();
        }

        List<RecommendationDto> result = new ArrayList<>();

        for (RuleEntity rule : rules) {
            boolean matched = rule.getQueries().stream()
                                  .allMatch(q -> queryEngine.evaluate(userId, q));

            if (matched) {
                UUID productUuid = parseProductId(rule.getProductId());
                result.add(new RecommendationDto(productUuid, rule.getProductName(), rule.getProductText()));
            }
        }

        return result;
    }

    private UUID parseProductId(String productId) {
        try {
            return UUID.fromString(productId);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Некорректный product_id в динамическом правиле. Ожидался UUID, получено: " + productId, e
            );
        }
    }
}