package ru.starbank.recommendation.api;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI configuration.
 */
@Configuration
public class OpenApiConfig {

    /**
     * OpenAPI bean.
     *
     * @return OpenAPI
     */
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().info(new Info()
                .title("StarBank Recommendation API")
                .version("1.0.0")
                .description("Recommendation service (Stage 1)"));
    }
}