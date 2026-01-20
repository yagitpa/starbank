package ru.starbank.recommendation.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.starbank.recommendation.domain.rules.entity.RuleEntity;

import java.util.List;

/**
 * Репозиторий для управления динамическими правилами рекомендаций.
 *
 * <p>Работает с rules DB (второй DataSource), так как пакет репозитория
 * подключён через {@code @EnableJpaRepositories} в {@code RulesDataSourceConfig}.</p>
 */
public interface RuleRepository extends JpaRepository<RuleEntity, Long> {

    /**
     * Загружает все правила вместе с их queries одним запросом (избавляемся от N+1).
     */
    @Query("select distinct r from RuleEntity r left join fetch r.queries")
    List<RuleEntity> findAllWithQueries();
}
