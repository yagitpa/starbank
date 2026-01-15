package ru.starbank.recommendation.domain.rules.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Сущность динамического правила рекомендации (таблица rules).
 *
 * <p>Правило описывает "какой продукт рекомендовать", а набор {@link RuleQueryEntity}
 * задаёт условия (queries), которые должны выполняться (AND-логика на уровне сервиса).</p>
 */
@Entity
@Table(name = "rules")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = "queries")
public class RuleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    /**
     * Название продукта (как в ТЗ Stage 2).
     */
    @Column(name = "product_name", nullable = false)
    private String productName;

    /**
     * Идентификатор продукта (строковый, приходит из ТЗ/бота).
     */
    @Column(name = "product_id", nullable = false, length = 64)
    private String productId;

    /**
     * Текст/описание продукта для рекомендации.
     */
    @Column(name = "product_text", nullable = false, columnDefinition = "text")
    private String productText;

    /**
     * Условия правила (таблица rule_queries).
     *
     * <p>cascade = ALL + orphanRemoval = true означает:
     * <ul>
     *     <li>при сохранении RuleEntity сохраняются и queries</li>
     *     <li>при удалении query из списка она удалится из БД</li>
     *     <li>при удалении правила удалятся все его queries (дополнительно это же делает FK CASCADE)</li>
     * </ul>
     */
    @OneToMany(
            mappedBy = "rule",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<RuleQueryEntity> queries = new ArrayList<>();

    /**
     * Конструктор по умолчанию нужен JPA.
     */
    protected RuleEntity() {
    }

    public RuleEntity(String productName, String productId, String productText) {
        this.productName = productName;
        this.productId = productId;
        this.productText = productText;
    }

    /**
     * Добавить условие к правилу с корректной установкой связи.
     */
    public void addQuery(RuleQueryEntity query) {
        Objects.requireNonNull(query, "query must not be null");
        query.setRule(this);
        this.queries.add(query);
    }

    /**
     * Удалить условие из правила с корректной очисткой связи.
     */
    public void removeQuery(RuleQueryEntity query) {
        if (query == null) {
            return;
        }
        this.queries.remove(query);
        query.setRule(null);
    }

    /**
     * Утилита: заменить список queries целиком (например, при маппинге из DTO).
     * Важно: используем orphanRemoval, поэтому лишние строки удалятся из БД.
     */
    public void replaceQueries(List<RuleQueryEntity> newQueries) {
        this.queries.forEach(q -> q.setRule(null));
        this.queries.clear();

        if (newQueries == null) {
            return;
        }
        for (RuleQueryEntity q : newQueries) {
            addQuery(q);
        }
    }
}