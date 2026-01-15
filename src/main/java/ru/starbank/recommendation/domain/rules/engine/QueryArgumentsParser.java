package ru.starbank.recommendation.domain.rules.engine;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * Парсит arguments из RuleQueryEntity (JSON-массив строк) в List<String>.
 */
@Component
public class QueryArgumentsParser {

    private static final TypeReference<List<String>> STRING_LIST_TYPE = new TypeReference<>() {};

    private final ObjectMapper objectMapper;

    public QueryArgumentsParser(ObjectMapper objectMapper) {
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper must not be null");
    }

    public List<String> parse(String argumentsJson) {
        try {
            return objectMapper.readValue(argumentsJson, STRING_LIST_TYPE);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Некорректный формат arguments (ожидался JSON-массив строк): " + argumentsJson, e);
        }
    }

    public String requireAt(List<String> args, int index, String name) {
        if (args.size() <= index) {
            throw new IllegalArgumentException("Не хватает arguments: требуется " + name + " на позиции " + index);
        }
        String value = args.get(index);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Пустой argument: " + name);
        }
        return value;
    }
}