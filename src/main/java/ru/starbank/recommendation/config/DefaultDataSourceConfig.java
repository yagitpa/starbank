package ru.starbank.recommendation.config;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Конфигурация основной (первой) БД "базы знаний".
 * <p>
 * Требование Stage 2: основная БД должна быть помечена как {@link Primary},
 * а DataSource должен собираться через {@link DataSourceProperties#initializeDataSourceBuilder()}.
 */
@Configuration
public class DefaultDataSourceConfig {

    /**
     * Считывает параметры основной БД из spring.datasource.*
     */
    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties defaultDataSourceProperties() {
        return new DataSourceProperties();
    }

    /**
     * Основной DataSource (БД знаний), должен быть @Primary.
     */
    @Bean(name = "defaultDataSource")
    @Primary
    public DataSource defaultDataSource(DataSourceProperties defaultDataSourceProperties) {
        return defaultDataSourceProperties
                .initializeDataSourceBuilder()
                .build();
    }
}