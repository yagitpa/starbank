package ru.starbank.recommendation.config;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import org.springframework.transaction.PlatformTransactionManager;

/**
 * Конфигурация второй БД (Read/Write), где хранятся динамические правила рекомендаций.
 *
 * <p>Мы разделяем JPA-контуры:
 * <ul>
 *   <li>rules DB имеет свой {@link DataSource}, EntityManagerFactory и TransactionManager</li>
 *   <li>JPA-репозитории ограничены rules-пакетом</li>
 * </ul>
 * <p>Настроено под структуру проекта:
 * <ul>
 *   <li>Entity: ru.starbank.recommendation.domain.rules.entity</li>
 *   <li>Repository: ru.starbank.recommendation.repository</li>
 * </ul>
 *
 * <p>Префикс настроек: spring.rules-datasource.*
 */
@Configuration
@EnableJpaRepositories(
        basePackages = "ru.starbank.recommendation.repository",
        entityManagerFactoryRef = "rulesEntityManagerFactory",
        transactionManagerRef = "rulesTransactionManager"
)
public class RulesDataSourceConfig {

    /**
     * Параметры второй БД из spring.rules-datasource.*
     */
    @Bean
    @ConfigurationProperties("spring.rules-datasource")
    public DataSourceProperties rulesDataSourceProperties() {
        return new DataSourceProperties();
    }

    /**
     * DataSource второй БД (правила).
     */
    @Bean(name = "rulesDataSource")
    public DataSource rulesDataSource(
            @Qualifier("rulesDataSourceProperties") DataSourceProperties properties
    ) {
        return properties.initializeDataSourceBuilder().build();
    }

    /**
     * EntityManagerFactory для rules DB.
     */
    @Bean(name = "rulesEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean rulesEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("rulesDataSource") DataSource rulesDataSource
    ) {
        Map<String, Object> jpaProps = new HashMap<>();
        // DDL должен быть через Liquibase, поэтому hbm2ddl выключен
        jpaProps.put("hibernate.hbm2ddl.auto", "none");

        return builder
                .dataSource(rulesDataSource)
                .packages("ru.starbank.recommendation.domain.rules.entity")
                .persistenceUnit("rules")
                .properties(jpaProps)
                .build();
    }

    /**
     * TransactionManager для rules DB.
     */
    @Bean(name = "rulesTransactionManager")
    public PlatformTransactionManager rulesTransactionManager(
            @Qualifier("rulesEntityManagerFactory") LocalContainerEntityManagerFactoryBean rulesEntityManagerFactory
    ) {
        assert rulesEntityManagerFactory.getObject() != null;
        return new JpaTransactionManager(rulesEntityManagerFactory.getObject());
    }
}