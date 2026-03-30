package com.runestone.expeval2.api;

import com.runestone.expeval2.environment.ExpressionEnvironmentBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Operadores 'between' e 'not between'")
class BetweenExpressionTest {

    // -------------------------------------------------------------------------
    // Numérico
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Numérico")
    class Numeric {

        @ParameterizedTest(name = "{0} → {1}")
        @CsvSource({
            "5 between 1 and 10,  true",
            "1 between 1 and 10,  true",
            "10 between 1 and 10, true",
            "0 between 1 and 10,  false",
            "11 between 1 and 10, false",
        })
        @DisplayName("limites inclusivos")
        void inclusiveBounds(String expression, boolean expected) {
            assertThat(LogicalExpression.compile(expression).compute()).isEqualTo(expected);
        }

        @ParameterizedTest(name = "{0} → {1}")
        @CsvSource({
            "5 not between 1 and 10,  false",
            "0 not between 1 and 10,  true",
            "11 not between 1 and 10, true",
        })
        @DisplayName("not between numérico")
        void notBetweenNumeric(String expression, boolean expected) {
            assertThat(LogicalExpression.compile(expression).compute()).isEqualTo(expected);
        }

        @Test
        @DisplayName("valor como variável externa")
        void valueAsExternalVariable() {
            var env = new ExpressionEnvironmentBuilder()
                    .registerExternalSymbol("age", BigDecimal.ZERO, true)
                    .build();
            assertThat(LogicalExpression.compile("age between 18 and 65", env).compute(Map.of("age", new BigDecimal("30")))).isTrue();
            assertThat(LogicalExpression.compile("age between 18 and 65", env).compute(Map.of("age", new BigDecimal("17")))).isFalse();
        }
    }

    // -------------------------------------------------------------------------
    // String
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("String")
    class StringRange {

        @Test
        @DisplayName("string dentro do intervalo retorna true")
        void stringInRangeReturnsTrue() {
            assertThat(LogicalExpression.compile("\"d\" between \"a\" and \"z\"").compute()).isTrue();
        }

        @Test
        @DisplayName("string igual ao limite inferior retorna true")
        void stringAtLowerBoundReturnsTrue() {
            assertThat(LogicalExpression.compile("\"a\" between \"a\" and \"z\"").compute()).isTrue();
        }

        @Test
        @DisplayName("string fora do intervalo retorna false")
        void stringOutsideRangeReturnsFalse() {
            assertThat(LogicalExpression.compile("\"z\" between \"a\" and \"m\"").compute()).isFalse();
        }

        @Test
        @DisplayName("string not between fora do intervalo retorna true")
        void stringNotBetweenOutsideRangeReturnsTrue() {
            assertThat(LogicalExpression.compile("\"z\" not between \"a\" and \"m\"").compute()).isTrue();
        }
    }

    // -------------------------------------------------------------------------
    // Date
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Date")
    class DateRange {

        @Test
        @DisplayName("data dentro do intervalo retorna true")
        void dateInRangeReturnsTrue() {
            assertThat(LogicalExpression.compile("2024-06-15 between 2024-01-01 and 2024-12-31").compute()).isTrue();
        }

        @Test
        @DisplayName("data igual ao limite inferior retorna true")
        void dateAtLowerBoundReturnsTrue() {
            assertThat(LogicalExpression.compile("2024-01-01 between 2024-01-01 and 2024-12-31").compute()).isTrue();
        }

        @Test
        @DisplayName("data fora do intervalo retorna false")
        void dateOutsideRangeReturnsFalse() {
            assertThat(LogicalExpression.compile("2023-12-31 between 2024-01-01 and 2024-12-31").compute()).isFalse();
        }

        @Test
        @DisplayName("data not between fora do intervalo retorna true")
        void dateNotBetweenOutsideRangeReturnsTrue() {
            assertThat(LogicalExpression.compile("2023-12-31 not between 2024-01-01 and 2024-12-31").compute()).isTrue();
        }
    }

    // -------------------------------------------------------------------------
    // Time
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Time")
    class TimeRange {

        @Test
        @DisplayName("hora dentro do intervalo retorna true")
        void timeInRangeReturnsTrue() {
            assertThat(LogicalExpression.compile("10:30 between 08:00 and 18:00").compute()).isTrue();
        }

        @Test
        @DisplayName("hora fora do intervalo retorna false")
        void timeOutsideRangeReturnsFalse() {
            assertThat(LogicalExpression.compile("07:59 between 08:00 and 18:00").compute()).isFalse();
        }

        @Test
        @DisplayName("hora not between fora do intervalo retorna true")
        void timeNotBetweenOutsideRangeReturnsTrue() {
            assertThat(LogicalExpression.compile("07:59 not between 08:00 and 18:00").compute()).isTrue();
        }
    }

    // -------------------------------------------------------------------------
    // Avaliação única da expressão de valor
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Avaliação única do valor")
    class SingleEvaluation {

        @Test
        @DisplayName("função como valor é chamada exatamente uma vez")
        void functionValueIsEvaluatedExactlyOnce() {
            AtomicInteger callCount = new AtomicInteger(0);
            var env = new ExpressionEnvironmentBuilder()
                    .registerInstanceProvider(new CountingProvider(callCount))
                    .build();
            boolean result = LogicalExpression.compile("counted() between 1 and 10", env).compute();
            assertThat(result).isTrue();
            assertThat(callCount).hasValue(1);
        }
    }

    public static final class CountingProvider {

        private final AtomicInteger counter;

        CountingProvider(AtomicInteger counter) {
            this.counter = counter;
        }

        public BigDecimal counted() {
            counter.incrementAndGet();
            return new BigDecimal("5");
        }
    }
}
