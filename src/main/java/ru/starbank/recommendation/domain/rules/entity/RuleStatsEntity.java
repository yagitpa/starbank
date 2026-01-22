package ru.starbank.recommendation.domain.rules.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.util.Objects;

/**
 * Статистика срабатываний динамического правила.
 *
 * <p>PK таблицы — rule_id. Он совпадает с id правила (RuleEntity).</p>
 *
 * <p><b>Важно для JPA:</b> при использовании {@link MapsId} нельзя вручную выставлять id (ruleId)
 * до persist, иначе Spring Data может выполнить merge/update вместо insert.(Отловлено на тестах)</p>
 */
@Entity
@Getter
@Setter
@Table(name = "rule_stats")
public class RuleStatsEntity {

    @Id
    @Column(name = "rule_id", nullable = false)
    private Long ruleId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "rule_id", nullable = false)
    private RuleEntity rule;

    @Column(name = "count", nullable = false)
    private long count;

    protected RuleStatsEntity() {
        // for JPA
    }

    public RuleStatsEntity(RuleEntity rule) {
        this.rule = Objects.requireNonNull(rule, "rule must not be null");
        // ruleId НЕ выставляем вручную — его заполнит @MapsId при persist
        this.count = 0L;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        RuleStatsEntity that = (RuleStatsEntity) o;
        return ruleId != null && Objects.equals(ruleId, that.ruleId);
    }

    @Override
    public int hashCode() {
        return Hibernate.getClass(this).hashCode();
    }
}