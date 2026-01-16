package ru.starbank.recommendation.domain.rules.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Objects;

/**
 * Сущность одного условия (query) внутри динамического правила (таблица rule_queries).
 *
 * <p>Поле {@code arguments} хранит аргументы в виде строки (обычно JSON-массив),
 * чтобы не привязываться к конкретной СУБД и типам JSONB.</p>
 */
@Entity
@Table(name = "rule_queries")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = "rule")
public class RuleQueryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    /**
     * Родительское правило.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rule_id", nullable = false)
    private RuleEntity rule;

    /**
     * Тип запроса/условия.
     * В БД хранится строкой (EnumType.STRING) в колонке "query".
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "query", nullable = false, length = 64)
    private QueryType query;

    /**
     * Аргументы условия (JSON-массив).
     * Пример: ["DEBIT", "WITHDRAW", ">", "10000"]
     */
    @Column(name = "arguments", nullable = false, columnDefinition = "text")
    private String arguments;

    /**
     * Инверсия результата условия.
     * Если true — итоговый результат query будет отрицаться.
     */
    @Column(name = "negate", nullable = false)
    private boolean negate;

    /**
     * Конструктор по умолчанию нужен JPA.
     */
    protected RuleQueryEntity() {
    }

    public RuleQueryEntity(QueryType query, String arguments, boolean negate) {
        this.query = Objects.requireNonNull(query, "query must not be null");
        this.arguments = Objects.requireNonNull(arguments, "arguments must not be null");
        this.negate = negate;
    }
}