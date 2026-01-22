package ru.starbank.recommendation.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import ru.starbank.recommendation.domain.dto.RecommendationResponseDto;
import ru.starbank.recommendation.service.RecommendationService;
import ru.starbank.recommendation.support.util.UuidParser;

import java.util.UUID;

/**
 * REST controller for recommendation endpoint.
 */
@Tag(name = "1. Recommendations", description = "Получение рекомендаций для пользователя")
@RestController
public class RecommendationController {
    private static final Logger log = LoggerFactory.getLogger(RecommendationController.class);


    private final RecommendationService recommendationService;

    /**
     * Constructor.
     *
     * @param recommendationService service
     */
    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    /**
     * Returns recommendations for a given user.
     *
     * @param userId raw user_id from path
     * @return recommendation response
     */
    @Operation(
            summary = "Получает рекомендации для пользователя",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Рекомендации отправлены",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = RecommendationResponseDto.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid user_id")
            }
    )
    @GetMapping(value = "/recommendation/{user_id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public RecommendationResponseDto getRecommendations(
            @Parameter(description = "User id (UUID)", required = true)
            @PathVariable("user_id") String userId
    ) {
        UUID parsed = UuidParser.parseUserId(userId);
        log.info("GET /recommendation for user_id={}", parsed);

        RecommendationResponseDto response = recommendationService.getRecommendations(parsed);
        log.info("Recommendations returned for user_id={}, count={}", parsed, response.recommendations().size());

        return response;
    }
}