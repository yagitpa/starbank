package ru.starbank.recommendation.domain.rules;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.starbank.recommendation.domain.dto.RecommendationDto;
import ru.starbank.recommendation.repository.RecommendationRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Recommendation rule set for product "Простой кредит".
 */
@Component
public class SimpleCreditRuleSet implements RecommendationRuleSet {
    private static final Logger log = LoggerFactory.getLogger(SimpleCreditRuleSet.class);

    private static final UUID PRODUCT_ID = UUID.fromString("ab138afb-f3ba-4a93-b74f-0fcee86d447f");
    private static final String PRODUCT_NAME = "Простой кредит";

    private final RecommendationRepository fixedRulesRepository;

    public SimpleCreditRuleSet(RecommendationRepository recommendationRepository) {
        this.fixedRulesRepository = recommendationRepository;
    }

    @Override
    public Optional<RecommendationDto> check(UUID userId) {
        if (!fixedRulesRepository.matchesSimpleCredit(userId)) {
            log.info("Rule matched: Simple Credit for user_id={}", userId);
            return Optional.empty();
        }
        return Optional.of(new RecommendationDto(PRODUCT_ID, PRODUCT_NAME, text()));
    }

    private String text() {
        return "Откройте мир выгодных кредитов с нами!\n\n"
                + "Ищете способ быстро и без лишних хлопот получить нужную сумму? Тогда наш выгодный кредит — именно то, что вам нужно! "
                + "Мы предлагаем низкие процентные ставки, гибкие условия и индивидуальный подход к каждому клиенту.\n\n"
                + "Почему выбирают нас:\n"
                + "— Быстрое рассмотрение заявки.\n"
                + "— Удобное оформление онлайн.\n"
                + "— Широкий выбор кредитных продуктов.\n\n"
                + "Не упустите возможность воспользоваться выгодными условиями кредитования!";
    }
}