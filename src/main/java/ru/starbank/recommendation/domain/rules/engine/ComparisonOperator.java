package ru.starbank.recommendation.domain.rules.engine;

/**
 * Операторы сравнения для динамических правил Stage 2.
 */
public enum ComparisonOperator {
    GT(">") {
        @Override public boolean apply(long left, long right) { return left > right; }
    },
    LT("<") {
        @Override public boolean apply(long left, long right) { return left < right; }
    },
    EQ("=") {
        @Override public boolean apply(long left, long right) { return left == right; }
    },
    GTE(">=") {
        @Override public boolean apply(long left, long right) { return left >= right; }
    },
    LTE("<=") {
        @Override public boolean apply(long left, long right) { return left <= right; }
    };

    private final String token;

    ComparisonOperator(String token) {
        this.token = token;
    }

    public abstract boolean apply(long left, long right);

    public static ComparisonOperator fromToken(String token) {
        for (ComparisonOperator op : values()) {
            if (op.token.equals(token)) {
                return op;
            }
        }
        throw new IllegalArgumentException("Неизвестный оператор сравнения: " + token);
    }
}