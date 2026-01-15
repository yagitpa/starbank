package ru.starbank.recommendation.domain.rules.entity;

/**
 * Типы запросов (условий) динамического правила.
 *
 * <p>Важно: названия enum должны совпадать с тем, что приходит в JSON от API /rule,
 * и с тем, что мы сохраняем в БД (EnumType.STRING).</p>
 */
public enum QueryType {

    /**
     * Пользователь является пользователем продукта определённого типа.
     * Пример: USER_OF "DEBIT"
     */
    USER_OF,

    /**
     * Пользователь "активный" пользователь продукта (по логике Stage 2).
     * Пример: ACTIVE_USER_OF "DEBIT"
     */
    ACTIVE_USER_OF,

    /**
     * Сравнение суммы транзакций по типу продукта/операции с порогом.
     * Пример: TRANSACTION_SUM_COMPARE ["DEBIT", "WITHDRAW", ">", "10000"]
     */
    TRANSACTION_SUM_COMPARE,

    /**
     * Сравнение суммы пополнений и списаний (deposit vs withdraw) по условиям Stage 2.
     */
    TRANSACTION_SUM_COMPARE_DEPOSIT_WITHDRAW
}