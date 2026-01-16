package ru.starbank.recommendation.domain.dto.rule;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ru.starbank.recommendation.domain.rules.entity.QueryType;

import java.util.List;

/**
 * DTO одного запроса (условия) внутри динамического правила.
 *
 * <p>Валидация здесь проверяет базовую структуру:
 * наличие query, наличие arguments и их количество в зависимости от типа query.</p>
 */
public record RuleQueryDto(
        @NotNull
        @JsonProperty("query")
        QueryType query,

        @NotNull
        @Size(min = 1)
        @JsonProperty("arguments")
        List<@NotBlank String> arguments,

        @NotNull
        @JsonProperty("negate")
        Boolean negate
) {

    /**
     * Проверяет количество arguments под тип query согласно ТЗ:
     * USER_OF, ACTIVE_USER_OF -> 1
     * TRANSACTION_SUM_COMPARE -> 4
     * TRANSACTION_SUM_COMPARE_DEPOSIT_WITHDRAW -> 2
     */
    @AssertTrue(message = "Некорректное количество arguments для указанного query")
    public boolean isArgumentsCountValid() {
        if (query == null || arguments == null) {
            return true;
        }

        return switch (query) {
            case USER_OF, ACTIVE_USER_OF -> arguments.size() == 1;
            case TRANSACTION_SUM_COMPARE -> arguments.size() == 4;
            case TRANSACTION_SUM_COMPARE_DEPOSIT_WITHDRAW -> arguments.size() == 2;
        };
    }
}
