package ru.starbank.recommendation.api;

import org.springframework.boot.info.BuildProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.starbank.recommendation.domain.dto.management.ManagementInfoDto;

/**
 * Технологический контроллер Stage 3 для получения информации о сервисе.
 */
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
    @GetMapping("/info")
    public ManagementInfoDto info() {
        return new ManagementInfoDto(
                buildProperties.getName(),
                buildProperties.getVersion()
        );
    }
}