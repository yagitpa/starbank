package ru.starbank.recommendation.domain.rules;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.starbank.recommendation.domain.dto.RecommendationDto;
import ru.starbank.recommendation.repository.jdbc.RecommendationRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Recommendation rule set for product "Top Saving".
 */
@Component
public class TopSavingRuleSet implements RecommendationRuleSet {
    private static final Logger log = LoggerFactory.getLogger(TopSavingRuleSet.class);

    private static final UUID PRODUCT_ID = UUID.fromString("59efc529-2fff-41af-baff-90ccd7402925");
    private static final String PRODUCT_NAME = "Top Saving";

    private final RecommendationRepository fixedRulesRepository;

    public TopSavingRuleSet(RecommendationRepository recommendationRepository) {
        this.fixedRulesRepository = recommendationRepository;
    }

    @Override
    public Optional<RecommendationDto> check(UUID userId) {
        if (!fixedRulesRepository.matchesTopSaving(userId)) {
            log.info("Rule matched: Top Saving for user_id={}", userId);
            return Optional.empty();
        }
        return Optional.of(new RecommendationDto(PRODUCT_ID, PRODUCT_NAME, text()));
    }

    private String text() {
        return "Откройте свою собственную «Копилку» с нашим банком! «Копилка» — это уникальный банковский инструмент, "
                + "который поможет вам легко и удобно накапливать деньги на важные цели. "
                + "Больше никаких забытых чеков и потерянных квитанций — всё под контролем!\n\n"
                + "Преимущества «Копилки»:\n\n"
                + "1) Накопление средств на конкретные цели.\n"
                + "2) Прозрачность и контроль.\n"
                + "3) Безопасность и надежность.\n\n"
                + "Начните использовать «Копилку» уже сегодня и станьте ближе к своим финансовым целям!";
    }
}