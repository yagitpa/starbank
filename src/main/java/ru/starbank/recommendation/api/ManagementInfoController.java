package ru.starbank.recommendation.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.boot.info.BuildProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.starbank.recommendation.domain.dto.management.ManagementInfoDto;

/**
 * Технологический контроллер Stage 3 для получения информации о сервисе.
 */
@Tag(name = "4. Service info", description = "Получение информации о сервисе")
@RestController
@RequestMapping("/management")
public class ManagementInfoController {

    private final BuildProperties buildProperties;

    public ManagementInfoController(BuildProperties buildProperties) {
        this.buildProperties = buildProperties;
    }

    /**
     * Возвращает имя и версию сервиса, извлечённые из build-info (pom.xml).
     */
    @Operation(summary = "Получает информацию о сервисе",
            description = "Возвращает имя сервиса и его версию",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Успешный запрос",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ManagementInfoDto.class)))
            })
    @GetMapping("/info")
    public ManagementInfoDto info() {
        return new ManagementInfoDto(
                buildProperties.getName(),
                buildProperties.getVersion()
        );
    }
}