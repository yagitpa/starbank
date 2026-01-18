package ru.starbank.recommendation.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Репозиторий поиска пользователя в "базе знаний" H2 по username (для Telegram-бота).
 */
@Repository
public class UserLookupRepository {

    private final JdbcTemplate jdbcTemplate;

    public UserLookupRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate, "jdbcTemplate must not be null");
    }

    /**
     * Ищет пользователей по username.
     *
     * <p>По ТЗ бота: если найдено 0 или >1 пользователей — считаем, что пользователь не найден.</p>
     */
    public List<BankUserRow> findByUsername(String username) {
        String sql = """
                SELECT id, first_name, last_name, username
                FROM users
                WHERE username = ?
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new BankUserRow(
                        UUID.fromString(rs.getString("id")),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("username")
                ),
                username
        );
    }

    /**
     * Результат выборки пользователя из БД (минимально нужные поля для бота).
     */
    public record BankUserRow(
            UUID id,
            String firstName,
            String lastName,
            String username
    ) {
    }
}