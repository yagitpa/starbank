package ru.starbank.recommendation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.starbank.recommendation.domain.rules.entity.RuleEntity;

/**
 * Репозиторий для управления динамическими правилами рекомендаций.
 *
 * <p>Работает с rules DB (второй DataSource), так как пакет репозитория
 * подключён через {@code @EnableJpaRepositories} в {@code RulesDataSourceConfig}.</p>
 */
public interface RuleRepository extends JpaRepository<RuleEntity, Long> {
}
