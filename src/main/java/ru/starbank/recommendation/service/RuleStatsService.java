package ru.starbank.recommendation.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.starbank.recommendation.domain.dto.rule.RuleStatDto;
import ru.starbank.recommendation.domain.dto.rule.RuleStatsResponseDto;
import ru.starbank.recommendation.repository.RuleStatsRepository;

import java.util.List;

/**
 * Сервис статистики срабатываний динамических правил.
 */
@Service
public class RuleStatsService {

    private static final Logger log = LoggerFactory.getLogger(RuleStatsService.class);

    private final RuleStatsRepository ruleStatsRepository;

    public RuleStatsService(RuleStatsRepository ruleStatsRepository) {
        this.ruleStatsRepository = ruleStatsRepository;
    }

    /**
     * Атомарно увеличивает счётчик правила на 1.
     *
     * <p>Делаем отдельную транзакцию, чтобы инкремент не зависел от внешних read-only контекстов.</p>
     */
    @Transactional(transactionManager = "rulesTransactionManager", propagation = Propagation.REQUIRES_NEW)
    public void increment(long ruleId) {
        int updated = ruleStatsRepository.increment(ruleId);
        if (updated != 1) {
            log.warn("Rule stats increment affected {} rows. rule_id={}", updated, ruleId);
        }
    }

    /**
     * Возвращает статистику по всем правилам, включая те,
     * по которым ещё не было срабатываний (count = 0).
     */
    @Transactional(readOnly = true, transactionManager = "rulesTransactionManager")
    public RuleStatsResponseDto getStats() {
        List<RuleStatDto> stats = ruleStatsRepository.findAllRuleStats()
                                                     .stream()
                                                     .map(p -> new RuleStatDto(
                                                             String.valueOf(p.ruleId()),
                                                             String.valueOf(p.count())
                                                     ))
                                                     .toList();

        return new RuleStatsResponseDto(stats);
    }
}