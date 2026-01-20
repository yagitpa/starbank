package ru.starbank.recommendation.config.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.starbank.recommendation.domain.dto.RecommendationResponseDto;
import ru.starbank.recommendation.repository.jdbc.RecommendationRepository;
import ru.starbank.recommendation.service.RecommendationService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.util.List;
import java.util.UUID;

/**
 * Local-only DB connectivity and golden users verification.
 *
 * <p>Disabled for 'test' profile to keep automated tests isolated from file-based H2.</p>
 */
@Profile("!test")
@Configuration
public class DatabaseSmokeCheck {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSmokeCheck.class);

    // Golden users from Stage_1_test_users.txt
    private static final UUID USER_INVEST_500 = UUID.fromString("cd515076-5d8a-44be-930e-8d4fcb79f42d");
    private static final UUID USER_TOP_SAVING = UUID.fromString("d4a4d619-9a0c-4fc5-b0cb-76c49409546b");
    private static final UUID USER_SIMPLE_CREDIT = UUID.fromString("1f9b149c-6577-448a-bc94-16bea229b71a");

    private static final String EXPECT_INVEST_500 = "Invest 500";
    private static final String EXPECT_TOP_SAVING = "Top Saving";
    private static final String EXPECT_SIMPLE_CREDIT = "Простой кредит";

    private static final BigDecimal KOPECKS_DIVISOR = BigDecimal.valueOf(100);

    @Bean
    public ApplicationRunner databaseSmokeCheckRunner(JdbcTemplate jdbcTemplate,
                                                      RecommendationRepository recommendationRepository,
                                                      RecommendationService recommendationService) {
        return args -> {
            printJdbcUrl(jdbcTemplate);
            printBasicCounts(jdbcTemplate);
            printDistinctTypes(jdbcTemplate);

            // Old probe (kept)
            printRawAmountProbeForInvest500(jdbcTemplate);

            // NEW: full decision inputs for all golden users
            printGoldenUsersDecisionInputs(jdbcTemplate);

            // Golden verification: repo rule + API output
            log.info("============================================================");
            log.info("Golden users verification (expected rules MUST match):");

            checkGoldenUser(jdbcTemplate, recommendationService,
                    USER_INVEST_500, EXPECT_INVEST_500,
                    recommendationRepository.matchesInvest500(USER_INVEST_500));

            checkGoldenUser(jdbcTemplate, recommendationService,
                    USER_TOP_SAVING, EXPECT_TOP_SAVING,
                    recommendationRepository.matchesTopSaving(USER_TOP_SAVING));

            checkGoldenUser(jdbcTemplate, recommendationService,
                    USER_SIMPLE_CREDIT, EXPECT_SIMPLE_CREDIT,
                    recommendationRepository.matchesSimpleCredit(USER_SIMPLE_CREDIT));

            log.info("============================================================");
        };
    }

    // -------------------------------------------------------------------------
    // Basic environment / DB checks
    // -------------------------------------------------------------------------

    private void printJdbcUrl(JdbcTemplate jdbcTemplate) {
        try (Connection c = jdbcTemplate.getDataSource().getConnection()) {
            log.info("JDBC URL = {}", c.getMetaData().getURL());
        } catch (Exception e) {
            log.warn("Failed to read JDBC URL from connection metadata", e);
        }
    }

    private void printBasicCounts(JdbcTemplate jdbcTemplate) {
        Integer users = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM public.users", Integer.class);
        Integer products = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM public.products", Integer.class);
        Integer transactions = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM public.transactions", Integer.class);

        log.info("DB OK. USERS={}, PRODUCTS={}, TRANSACTIONS={}", users, products, transactions);
    }

    private void printDistinctTypes(JdbcTemplate jdbcTemplate) {
        log.info("DB distinct product types:");
        jdbcTemplate.queryForList("SELECT DISTINCT p.type AS t FROM public.products p ORDER BY 1")
                    .forEach(row -> log.info(" - {}", row.get("T")));

        log.info("DB distinct transaction types:");
        jdbcTemplate.queryForList("SELECT DISTINCT t.type AS t FROM public.transactions t ORDER BY 1")
                    .forEach(row -> log.info(" - {}", row.get("T")));
    }

    /**
     * Raw probe for Invest500 user: SAVING deposits sum (DB units) and /100 as RUB if DB stores kopecks.
     */
    private void printRawAmountProbeForInvest500(JdbcTemplate jdbcTemplate) {
        Long rawSavingDeposits = jdbcTemplate.queryForObject("""
            SELECT COALESCE(SUM(t.amount), 0)
            FROM transactions t
            JOIN products p ON p.id = t.product_id
            WHERE t.user_id = ?
              AND p.type = 'SAVING'
              AND t.type = 'DEPOSIT'
            """, Long.class, USER_INVEST_500);

        if (rawSavingDeposits == null) {
            rawSavingDeposits = 0L;
        }

        double asRublesIfKopecks = rawSavingDeposits / 100.0;

        log.info("RAW amount probe (Invest500 user, SAVING DEPOSIT): rawSum(DB units)={} | raw/100={} (RUB if DB stores kopecks)",
                rawSavingDeposits, asRublesIfKopecks);
    }

    // -------------------------------------------------------------------------
    // Decision inputs report (what numbers actually participate in rules)
    // -------------------------------------------------------------------------

    private void printGoldenUsersDecisionInputs(JdbcTemplate jdbcTemplate) {
        log.info("============================================================");
        log.info("Decision inputs (amounts in KOPECKS + RUB) for golden users:");

        printDecisionInputsForUser(jdbcTemplate, USER_INVEST_500, "Invest500 golden user");
        printDecisionInputsForUser(jdbcTemplate, USER_TOP_SAVING, "TopSaving golden user");
        printDecisionInputsForUser(jdbcTemplate, USER_SIMPLE_CREDIT, "SimpleCredit golden user");

        log.info("============================================================");
    }

    /**
     * Prints full profile of metrics used by Stage 1 rules.
     */
    private void printDecisionInputsForUser(JdbcTemplate jdbcTemplate, UUID userId, String label) {
        log.info("------------------------------------------------------------");
        log.info("Decision inputs for {}: user_id={}", label, userId);

        boolean hasDebit = hasAnyProductOfType(jdbcTemplate, userId, "DEBIT");
        boolean hasSaving = hasAnyProductOfType(jdbcTemplate, userId, "SAVING");
        boolean hasInvest = hasAnyProductOfType(jdbcTemplate, userId, "INVEST");
        boolean hasCredit = hasAnyProductOfType(jdbcTemplate, userId, "CREDIT");

        log.info("Product usage flags: hasDEBIT={}, hasSAVING={}, hasINVEST={}, hasCREDIT={}",
                hasDebit, hasSaving, hasInvest, hasCredit);

        Money debitDeposit = sumAmount(jdbcTemplate, userId, "DEBIT", "DEPOSIT");
        Money debitWithdraw = sumAmount(jdbcTemplate, userId, "DEBIT", "WITHDRAW");
        Money savingDeposit = sumAmount(jdbcTemplate, userId, "SAVING", "DEPOSIT");

        log.info("Sums by rule inputs:");
        logMoney("DEBIT  DEPOSIT", debitDeposit);
        logMoney("DEBIT  WITHDRAW", debitWithdraw);
        logMoney("SAVING DEPOSIT", savingDeposit);

        Money debitBalance = debitDeposit.minus(debitWithdraw);
        logMoney("DEBIT balance (deposit - withdraw)", debitBalance);

        // "Human-readable" check in RUB just for explanation in logs
        boolean invest500ByLogicRub = hasDebit && !hasInvest
                && savingDeposit.rub.compareTo(BigDecimal.valueOf(1000)) > 0;

        boolean topSavingByLogicRub = hasDebit
                && (debitDeposit.rub.compareTo(BigDecimal.valueOf(50_000)) >= 0
                || savingDeposit.rub.compareTo(BigDecimal.valueOf(50_000)) >= 0)
                && debitBalance.rub.compareTo(BigDecimal.ZERO) > 0;

        boolean simpleCreditByLogicRub = !hasCredit
                && debitBalance.rub.compareTo(BigDecimal.ZERO) > 0
                && debitWithdraw.rub.compareTo(BigDecimal.valueOf(100_000)) > 0;

        log.info("Logic check in RUB (for explanation): Invest500={}, TopSaving={}, SimpleCredit={}",
                invest500ByLogicRub, topSavingByLogicRub, simpleCreditByLogicRub);
    }

    private boolean hasAnyProductOfType(JdbcTemplate jdbcTemplate, UUID userId, String productType) {
        Boolean result = jdbcTemplate.queryForObject("""
            SELECT EXISTS (
                SELECT 1
                FROM transactions t
                JOIN products p ON p.id = t.product_id
                WHERE t.user_id = ? AND p.type = ?
            ) AS result
            """, Boolean.class, userId, productType);

        return Boolean.TRUE.equals(result);
    }

    private Money sumAmount(JdbcTemplate jdbcTemplate, UUID userId, String productType, String transactionType) {
        Long raw = jdbcTemplate.queryForObject("""
            SELECT COALESCE(SUM(t.amount), 0)
            FROM transactions t
            JOIN products p ON p.id = t.product_id
            WHERE t.user_id = ?
              AND p.type = ?
              AND t.type = ?
            """, Long.class, userId, productType, transactionType);

        long rawKop = raw == null ? 0L : raw;

        BigDecimal rub = BigDecimal.valueOf(rawKop)
                                   .divide(KOPECKS_DIVISOR, 2, RoundingMode.HALF_UP);

        return new Money(rawKop, rub);
    }

    private void logMoney(String label, Money money) {
        log.info("{}: rawKop={} | rub={}", label, money.rawKop, money.rub);
    }

    private static final class Money {
        private final long rawKop;
        private final BigDecimal rub;

        private Money(long rawKop, BigDecimal rub) {
            this.rawKop = rawKop;
            this.rub = rub;
        }

        private Money minus(Money other) {
            long raw = this.rawKop - other.rawKop;
            BigDecimal rub = this.rub.subtract(other.rub);
            return new Money(raw, rub);
        }
    }

    // -------------------------------------------------------------------------
    // Golden users verification: repo rule + API response
    // -------------------------------------------------------------------------

    private void checkGoldenUser(JdbcTemplate jdbcTemplate,
                                 RecommendationService recommendationService,
                                 UUID userId,
                                 String expectedRecommendationName,
                                 boolean repoRuleResult) {

        Boolean userExists = jdbcTemplate.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM public.users u WHERE u.id = ?)",
                Boolean.class,
                userId
        );

        Integer txCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM public.transactions t WHERE t.user_id = ?",
                Integer.class,
                userId
        );

        RecommendationResponseDto apiResponse = recommendationService.getRecommendations(userId);
        List<String> apiNames = apiResponse.recommendations().stream().map(r -> r.name()).toList();
        boolean apiHasExpected = apiNames.contains(expectedRecommendationName);

        log.info("------------------------------------------------------------");
        log.info("User={} expected='{}'", userId, expectedRecommendationName);
        log.info("exists={}, txCount={}", Boolean.TRUE.equals(userExists), txCount);
        log.info("Repo rule result={}", repoRuleResult);
        log.info("API recommendations={}", apiNames);

        if (!Boolean.TRUE.equals(userExists)) {
            log.warn("FAIL: user does not exist in USERS table (check DB file / connection URL).");
            return;
        }
        if (txCount == null || txCount == 0) {
            log.warn("FAIL: user has 0 transactions (check DB file / test users insertion).");
            return;
        }
        if (!repoRuleResult) {
            log.warn("FAIL: expected rule returned FALSE in repository.");
        } else {
            log.info("OK: repository rule returned TRUE.");
        }
        if (!apiHasExpected) {
            log.warn("FAIL: expected recommendation '{}' is NOT returned by API.", expectedRecommendationName);
        } else {
            log.info("OK: expected recommendation '{}' is returned by API.", expectedRecommendationName);
        }
    }
}