package ru.starbank.recommendation.config.bot;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Включает бин TelegramBotProperties.
 */
@Configuration
@EnableConfigurationProperties(TelegramBotProperties.class)
public class TelegramBotPropertiesConfig {
}