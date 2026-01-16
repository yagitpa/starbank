package ru.starbank.recommendation.domain.rules.engine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ComparisonOperatorTest {

    @Test
    void fromToken_shouldResolveAllSupportedOperators() {
        assertEquals(ComparisonOperator.GT, ComparisonOperator.fromToken(">"));
        assertEquals(ComparisonOperator.LT, ComparisonOperator.fromToken("<"));
        assertEquals(ComparisonOperator.EQ, ComparisonOperator.fromToken("="));
        assertEquals(ComparisonOperator.GTE, ComparisonOperator.fromToken(">="));
        assertEquals(ComparisonOperator.LTE, ComparisonOperator.fromToken("<="));
    }

    @Test
    void fromToken_shouldThrowOnUnknownOperator() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> ComparisonOperator.fromToken(">>>")
        );
        assertTrue(ex.getMessage().contains("Неизвестный оператор"));
    }

    @Test
    void apply_shouldWorkCorrectly() {
        assertTrue(ComparisonOperator.GT.apply(11, 10));
        assertFalse(ComparisonOperator.GT.apply(10, 10));

        assertTrue(ComparisonOperator.LT.apply(9, 10));
        assertFalse(ComparisonOperator.LT.apply(10, 10));

        assertTrue(ComparisonOperator.EQ.apply(10, 10));
        assertFalse(ComparisonOperator.EQ.apply(10, 11));

        assertTrue(ComparisonOperator.GTE.apply(10, 10));
        assertTrue(ComparisonOperator.GTE.apply(11, 10));
        assertFalse(ComparisonOperator.GTE.apply(9, 10));

        assertTrue(ComparisonOperator.LTE.apply(10, 10));
        assertTrue(ComparisonOperator.LTE.apply(9, 10));
        assertFalse(ComparisonOperator.LTE.apply(11, 10));
    }
}