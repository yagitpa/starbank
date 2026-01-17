package ru.starbank.recommendation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.starbank.recommendation.domain.rules.entity.RuleStatsEntity;

import java.util.List;

/**
 * Репозиторий статистики срабатываний динамических правил.
 */
@Repository
public interface RuleStatsRepository extends JpaRepository<RuleStatsEntity, Long> {

    /**
     * Атомарно увеличивает счётчик срабатываний правила на 1.
     *
     * @param ruleId id правила
     * @return количество обновлённых строк (ожидаем 1)
     */
    @Modifying
    @Query(value = "UPDATE rule_stats SET count = count + 1 WHERE rule_id = :ruleId", nativeQuery = true)
    int increment(@Param("ruleId") long ruleId);

    /**
     * Возвращает статистику по всем правилам, включая те, у которых count=0.
     */
    @Query(value = """
            SELECT r.id AS ruleId, COALESCE(s.count, 0) AS count
            FROM rules r
            LEFT JOIN rule_stats s ON s.rule_id = r.id
            ORDER BY r.id
            """, nativeQuery = true)
    List<RuleStatProjection> findAllRuleStats();

    /**
     * Проекция для native-запроса статистики.
     */
    interface RuleStatProjection {
        Long getRuleId();
        Long getCount();
    }
}