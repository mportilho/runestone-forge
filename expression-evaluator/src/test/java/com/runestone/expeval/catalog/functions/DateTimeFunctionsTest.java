package com.runestone.expeval.catalog.functions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.temporal.ChronoField;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("DateTimeFunctions Tests")
class DateTimeFunctionsTest {

    private final LocalDateTime baseDateTime = LocalDateTime.of(2023, 5, 15, 10, 30, 45);

    @Nested
    @DisplayName("set* methods")
    class SetMethodsTests {

        @Test
        @DisplayName("setDay()")
        void setDay() {
            var result = DateTimeFunctions.setDay(baseDateTime, 20);
            assertThat(result.get(ChronoField.DAY_OF_MONTH)).isEqualTo(20);
        }

        @Test
        @DisplayName("setMonth()")
        void setMonth() {
            var result = DateTimeFunctions.setMonth(baseDateTime, 12);
            assertThat(result.get(ChronoField.MONTH_OF_YEAR)).isEqualTo(12);
        }

        @Test
        @DisplayName("setYear()")
        void setYear() {
            var result = DateTimeFunctions.setYear(baseDateTime, 2025);
            assertThat(result.get(ChronoField.YEAR)).isEqualTo(2025);
        }

        @Test
        @DisplayName("setHours()")
        void setHours() {
            var result = DateTimeFunctions.setHours(baseDateTime, 22);
            assertThat(result.get(ChronoField.HOUR_OF_DAY)).isEqualTo(22);
        }

        @Test
        @DisplayName("setMinutes()")
        void setMinutes() {
            var result = DateTimeFunctions.setMinutes(baseDateTime, 59);
            assertThat(result.get(ChronoField.MINUTE_OF_HOUR)).isEqualTo(59);
        }

        @Test
        @DisplayName("setSeconds()")
        void setSeconds() {
            var result = DateTimeFunctions.setSeconds(baseDateTime, 15);
            assertThat(result.get(ChronoField.SECOND_OF_MINUTE)).isEqualTo(15);
        }

        @Test
        @DisplayName("setMidnight()")
        void setMidnight() {
            var result = DateTimeFunctions.setMidnight(baseDateTime);
            assertThat(result.get(ChronoField.HOUR_OF_DAY)).isZero();
            assertThat(result.get(ChronoField.MINUTE_OF_HOUR)).isZero();
            assertThat(result.get(ChronoField.SECOND_OF_MINUTE)).isZero();
            assertThat(result.get(ChronoField.NANO_OF_SECOND)).isZero();
        }

        @Test
        @DisplayName("setMidday()")
        void setMidday() {
            var result = DateTimeFunctions.setMidday(baseDateTime);
            assertThat(result.get(ChronoField.HOUR_OF_DAY)).isEqualTo(12);
            assertThat(result.get(ChronoField.MINUTE_OF_HOUR)).isZero();
            assertThat(result.get(ChronoField.SECOND_OF_MINUTE)).isZero();
            assertThat(result.get(ChronoField.NANO_OF_SECOND)).isZero();
        }

        @Test
        @DisplayName("Throws exception on invalid field value")
        void invalidFieldValue() {
            assertThatThrownBy(() -> DateTimeFunctions.setMonth(baseDateTime, 13))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("add* methods")
    class AddMethodsTests {

        @Test
        @DisplayName("addDay()")
        void addDay() {
            var result = DateTimeFunctions.addDay(baseDateTime, 2);
            assertThat(result).isEqualTo(baseDateTime.plusDays(2));
        }

        @Test
        @DisplayName("addMonth()")
        void addMonth() {
            var result = DateTimeFunctions.addMonth(baseDateTime, 2);
            assertThat(result).isEqualTo(baseDateTime.plusMonths(2));
        }

        @Test
        @DisplayName("addYear()")
        void addYear() {
            var result = DateTimeFunctions.addYear(baseDateTime, 2);
            assertThat(result).isEqualTo(baseDateTime.plusYears(2));
        }

        @Test
        @DisplayName("addHours()")
        void addHours() {
            var result = DateTimeFunctions.addHours(baseDateTime, 2);
            assertThat(result).isEqualTo(baseDateTime.plusHours(2));
        }

        @Test
        @DisplayName("addMinutes()")
        void addMinutes() {
            var result = DateTimeFunctions.addMinutes(baseDateTime, 2);
            assertThat(result).isEqualTo(baseDateTime.plusMinutes(2));
        }

        @Test
        @DisplayName("addSeconds()")
        void addSeconds() {
            var result = DateTimeFunctions.addSeconds(baseDateTime, 2);
            assertThat(result).isEqualTo(baseDateTime.plusSeconds(2));
        }
    }

    @Nested
    @DisplayName("sub* methods")
    class SubMethodsTests {

        @Test
        @DisplayName("subDay()")
        void subDay() {
            var result = DateTimeFunctions.subDay(baseDateTime, 2);
            assertThat(result).isEqualTo(baseDateTime.minusDays(2));
        }

        @Test
        @DisplayName("subMonth()")
        void subMonth() {
            var result = DateTimeFunctions.subMonth(baseDateTime, 2);
            assertThat(result).isEqualTo(baseDateTime.minusMonths(2));
        }

        @Test
        @DisplayName("subYear()")
        void subYear() {
            var result = DateTimeFunctions.subYear(baseDateTime, 2);
            assertThat(result).isEqualTo(baseDateTime.minusYears(2));
        }

        @Test
        @DisplayName("subHours()")
        void subHours() {
            var result = DateTimeFunctions.subHours(baseDateTime, 2);
            assertThat(result).isEqualTo(baseDateTime.minusHours(2));
        }

        @Test
        @DisplayName("subMinutes()")
        void subMinutes() {
            var result = DateTimeFunctions.subMinutes(baseDateTime, 2);
            assertThat(result).isEqualTo(baseDateTime.minusMinutes(2));
        }

        @Test
        @DisplayName("subSeconds()")
        void subSeconds() {
            var result = DateTimeFunctions.subSeconds(baseDateTime, 2);
            assertThat(result).isEqualTo(baseDateTime.minusSeconds(2));
        }
    }

    @Nested
    @DisplayName("Between methods (existing)")
    class BetweenMethodsTests {

        private final LocalDateTime t1 = LocalDateTime.of(2023, 1, 1, 0, 0, 0);
        private final LocalDateTime t2 = LocalDateTime.of(2024, 2, 2, 1, 1, 1);

        @Test
        @DisplayName("secondsBetween()")
        void secondsBetween() {
            assertThat(DateTimeFunctions.secondsBetween(t1, t2)).isEqualTo(34304461L);
        }

        @Test
        @DisplayName("minutesBetween()")
        void minutesBetween() {
            assertThat(DateTimeFunctions.minutesBetween(t1, t2)).isEqualTo(571741L);
        }

        @Test
        @DisplayName("hoursBetween()")
        void hoursBetween() {
            assertThat(DateTimeFunctions.hoursBetween(t1, t2)).isEqualTo(9529L);
        }

        @Test
        @DisplayName("daysBetween()")
        void daysBetween() {
            assertThat(DateTimeFunctions.daysBetween(t1, t2)).isEqualTo(397L);
        }

        @Test
        @DisplayName("monthsBetween()")
        void monthsBetween() {
            assertThat(DateTimeFunctions.monthsBetween(t1, t2)).isEqualTo(13L);
        }

        @Test
        @DisplayName("yearsBetween()")
        void yearsBetween() {
            assertThat(DateTimeFunctions.yearsBetween(t1, t2)).isEqualTo(1L);
        }
    }
}
