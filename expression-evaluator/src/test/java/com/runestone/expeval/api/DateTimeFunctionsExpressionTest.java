package com.runestone.expeval.api;

import com.runestone.expeval.environment.ExpressionEnvironment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

// All DateTimeFunctions work via the expression API. Temporal parameters map to UnknownType.INSTANCE,
// so semantic type-checking is skipped, and coercion step 3 (targetType.isInstance(value))
// succeeds for LocalDate and LocalDateTime since both implement Temporal.
// Date literals (DATE token, e.g. 2024-01-01) and datetime literals (DATETIME token, e.g. 2024-01-01T10:00)
// are valid function-call arguments via the grammar's allEntityTypes rule. See runtime-internals.md §8.
@DisplayName("DateTimeFunctions via MathExpression/LogicalExpression API")
class DateTimeFunctionsExpressionTest {

    private static final ExpressionEnvironment ENV = ExpressionEnvironment.builder().addDateTimeFunctions().build();

    @Nested
    @DisplayName("daysBetween")
    class DaysBetween {

        @Test
        @DisplayName("daysBetween with date literals: 2024-01-01 to 2024-12-31 = 365")
        void daysBetweenDateLiterals() {
            BigDecimal result = MathExpression.compile("daysBetween(2024-01-01, 2024-12-31)", ENV).compute();
            assertThat(result).isEqualByComparingTo(new BigDecimal("365"));
        }

        @Test
        @DisplayName("daysBetween with variable LocalDate values")
        void daysBetweenVariables() {
            BigDecimal result = MathExpression.compile("daysBetween(d1, d2)", ENV)
                    .compute(Map.of("d1", LocalDate.of(2024, 1, 1), "d2", LocalDate.of(2024, 12, 31)));
            assertThat(result).isEqualByComparingTo(new BigDecimal("365"));
        }

        @Test
        @DisplayName("daysBetween of same date = 0")
        void daysBetweenSameDate() {
            BigDecimal result = MathExpression.compile("daysBetween(d1, d2)", ENV)
                    .compute(Map.of("d1", LocalDate.of(2024, 6, 15), "d2", LocalDate.of(2024, 6, 15)));
            assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("daysBetween in logical expression: days in 2024 > 300 → true")
        void daysBetweenInLogicalExpression() {
            boolean result = LogicalExpression.compile("daysBetween(2024-01-01, 2024-12-31) > 300", ENV).compute();
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("monthsBetween")
    class MonthsBetween {

        @Test
        @DisplayName("monthsBetween with date literals: one full year = 12")
        void monthsBetweenOneYear() {
            BigDecimal result = MathExpression.compile("monthsBetween(2023-01-01, 2024-01-01)", ENV).compute();
            assertThat(result).isEqualByComparingTo(new BigDecimal("12"));
        }

        @Test
        @DisplayName("monthsBetween with variable dates: 6 months")
        void monthsBetweenSixMonths() {
            BigDecimal result = MathExpression.compile("monthsBetween(d1, d2)", ENV)
                    .compute(Map.of("d1", LocalDate.of(2024, 1, 1), "d2", LocalDate.of(2024, 7, 1)));
            assertThat(result).isEqualByComparingTo(new BigDecimal("6"));
        }

        @Test
        @DisplayName("monthsBetween within same calendar month = 0")
        void monthsBetweenSameMonth() {
            BigDecimal result = MathExpression.compile("monthsBetween(d1, d2)", ENV)
                    .compute(Map.of("d1", LocalDate.of(2024, 6, 1), "d2", LocalDate.of(2024, 6, 30)));
            assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("monthsBetween in logical expression: 12 months in a year ≥ 12 → true")
        void monthsBetweenInLogicalExpression() {
            boolean result = LogicalExpression.compile("monthsBetween(2023-01-01, 2024-01-01) >= 12", ENV).compute();
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("yearsBetween")
    class YearsBetween {

        @Test
        @DisplayName("yearsBetween with date literals: 2 full years")
        void yearsBetweenTwoYears() {
            BigDecimal result = MathExpression.compile("yearsBetween(2022-01-01, 2024-01-01)", ENV).compute();
            assertThat(result).isEqualByComparingTo(new BigDecimal("2"));
        }

        @Test
        @DisplayName("yearsBetween with variable dates: 24 years")
        void yearsBetweenVariables() {
            BigDecimal result = MathExpression.compile("yearsBetween(d1, d2)", ENV)
                    .compute(Map.of("d1", LocalDate.of(2000, 1, 1), "d2", LocalDate.of(2024, 1, 1)));
            assertThat(result).isEqualByComparingTo(new BigDecimal("24"));
        }

        @Test
        @DisplayName("yearsBetween within same year = 0")
        void yearsBetweenSameYear() {
            BigDecimal result = MathExpression.compile("yearsBetween(d1, d2)", ENV)
                    .compute(Map.of("d1", LocalDate.of(2024, 1, 1), "d2", LocalDate.of(2024, 12, 31)));
            assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("yearsBetween in logical expression: minimum age check ≥ 18 → true")
        void yearsBetweenInLogicalExpression() {
            boolean result = LogicalExpression.compile("yearsBetween(2000-01-01, 2024-01-01) >= 18", ENV).compute();
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("hoursBetween")
    class HoursBetween {

        @Test
        @DisplayName("hoursBetween with datetime literals: 1 day apart = 24 hours")
        void hoursBetweenOneDayApart() {
            BigDecimal result = MathExpression.compile("hoursBetween(2024-01-01T00:00, 2024-01-02T00:00)", ENV).compute();
            assertThat(result).isEqualByComparingTo(new BigDecimal("24"));
        }

        @Test
        @DisplayName("hoursBetween with variable LocalDateTime: 12 hours apart")
        void hoursBetweenVariables() {
            BigDecimal result = MathExpression.compile("hoursBetween(dt1, dt2)", ENV)
                    .compute(Map.of("dt1", LocalDateTime.of(2024, 1, 1, 8, 0), "dt2", LocalDateTime.of(2024, 1, 1, 20, 0)));
            assertThat(result).isEqualByComparingTo(new BigDecimal("12"));
        }

        @Test
        @DisplayName("hoursBetween same datetime = 0")
        void hoursBetweenSameDatetime() {
            BigDecimal result = MathExpression.compile("hoursBetween(dt1, dt2)", ENV)
                    .compute(Map.of("dt1", LocalDateTime.of(2024, 6, 15, 10, 30), "dt2", LocalDateTime.of(2024, 6, 15, 10, 30)));
            assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("minutesBetween")
    class MinutesBetween {

        @Test
        @DisplayName("minutesBetween with datetime literals: 1 hour = 60 minutes")
        void minutesBetweenOneHour() {
            BigDecimal result = MathExpression.compile("minutesBetween(2024-01-01T10:00, 2024-01-01T11:00)", ENV).compute();
            assertThat(result).isEqualByComparingTo(new BigDecimal("60"));
        }

        @Test
        @DisplayName("minutesBetween with variable LocalDateTime: 30 minutes apart")
        void minutesBetweenVariables() {
            BigDecimal result = MathExpression.compile("minutesBetween(dt1, dt2)", ENV)
                    .compute(Map.of("dt1", LocalDateTime.of(2024, 1, 1, 9, 0), "dt2", LocalDateTime.of(2024, 1, 1, 9, 30)));
            assertThat(result).isEqualByComparingTo(new BigDecimal("30"));
        }

        @Test
        @DisplayName("minutesBetween in logical expression: 60 minutes in an hour = 60 → true")
        void minutesBetweenInLogicalExpression() {
            boolean result = LogicalExpression.compile("minutesBetween(2024-01-01T10:00, 2024-01-01T11:00) = 60", ENV).compute();
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("secondsBetween")
    class SecondsBetween {

        @Test
        @DisplayName("secondsBetween with datetime literals: 1 minute = 60 seconds")
        void secondsBetweenOneMinute() {
            BigDecimal result = MathExpression.compile("secondsBetween(2024-01-01T10:00, 2024-01-01T10:01)", ENV).compute();
            assertThat(result).isEqualByComparingTo(new BigDecimal("60"));
        }

        @Test
        @DisplayName("secondsBetween with variable LocalDateTime: 90 seconds apart")
        void secondsBetweenVariables() {
            BigDecimal result = MathExpression.compile("secondsBetween(dt1, dt2)", ENV)
                    .compute(Map.of("dt1", LocalDateTime.of(2024, 1, 1, 0, 0, 0), "dt2", LocalDateTime.of(2024, 1, 1, 0, 1, 30)));
            assertThat(result).isEqualByComparingTo(new BigDecimal("90"));
        }

        @Test
        @DisplayName("secondsBetween same datetime = 0")
        void secondsBetweenSameDatetime() {
            BigDecimal result = MathExpression.compile("secondsBetween(dt1, dt2)", ENV)
                    .compute(Map.of("dt1", LocalDateTime.of(2024, 6, 1, 12, 0, 0), "dt2", LocalDateTime.of(2024, 6, 1, 12, 0, 0)));
            assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }
}
