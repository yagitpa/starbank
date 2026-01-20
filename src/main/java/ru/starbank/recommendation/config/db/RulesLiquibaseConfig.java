package ru.starbank.recommendation.config.db;

import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * Liquibase для второй БД (rules DB).
 *
 * <p>Смысл: миграции динамических правил применяются ТОЛЬКО к rulesDataSource,
 * а основная H2 (knowledge DB) остаётся без изменений.</p>
 */
@Configuration
public class RulesLiquibaseConfig {

    @Bean
    public SpringLiquibase rulesLiquibase(@Qualifier("rulesDataSource") DataSource rulesDataSource) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(rulesDataSource);
        liquibase.setChangeLog("classpath:/db/changelog/db.changelog-rules.yaml");

        return liquibase;
    }
}