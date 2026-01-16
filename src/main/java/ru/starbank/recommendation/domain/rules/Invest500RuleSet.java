package ru.starbank.recommendation.domain.rules;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.starbank.recommendation.domain.dto.RecommendationDto;
import ru.starbank.recommendation.repository.RecommendationRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Recommendation rule set for product "Invest 500".
 */
@Component
public class Invest500RuleSet implements RecommendationRuleSet {
    private static final Logger log = LoggerFactory.getLogger(Invest500RuleSet.class);

    private static final UUID PRODUCT_ID = UUID.fromString("147f6a0f-3b91-413b-ab99-87f081d60d5a");
    private static final String PRODUCT_NAME = "Invest 500";

    private final RecommendationRepository fixedRulesRepository;

    public Invest500RuleSet(RecommendationRepository recommendationRepository) {
        this.fixedRulesRepository = recommendationRepository;
    }

    @Override
    public Optional<RecommendationDto> check(UUID userId) {
        if (!fixedRulesRepository.matchesInvest500(userId)) {
            log.info("Rule matched: Invest 500 for user_id={}", userId);
            return Optional.empty();
        }
        return Optional.of(new RecommendationDto(PRODUCT_ID, PRODUCT_NAME, text()));
    }

    private String text() {
        return "Откройте свой путь к успеху с индивидуальным инвестиционным счетом (ИИС) от нашего банка! "
                + "Воспользуйтесь налоговыми льготами и начните инвестировать с умом. "
                + "Пополните счет до конца года и получите выгоду в виде вычета на взнос в следующем налоговом периоде. "
                + "Не упустите возможность разнообразить свой портфель, снизить риски и следить за актуальными рыночными тенденциями. "
                + "Откройте ИИС сегодня и станьте ближе к финансовой независимости!";
    }
}