package ru.starbank.recommendation.config.openapi;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация OpenAPI (Swagger).
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI recommendationServiceOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("StarBank Recommendation Service")
                        .description("""
                                Сервис рекомендаций банковских продуктов.

                                Stage 2:
                                - динамические правила (/rule)
                                - rule engine (4 типа query)
                                - кеширование (Caffeine)
                                - обратная совместимость /recommendation
                                """)
                        .version("stage-2"));
    }
}