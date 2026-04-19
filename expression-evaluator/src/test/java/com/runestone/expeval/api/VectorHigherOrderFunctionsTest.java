package com.runestone.expeval.api;

import com.runestone.expeval.environment.ExpressionEnvironment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Vector higher-order functions: sum/prod with lambda transforms")
class VectorHigherOrderFunctionsTest {

    private static final List<BigDecimal> NUMS_1_2_3 = List.of(
            BigDecimal.ONE,
            new BigDecimal("2"),
            new BigDecimal("3")
    );

    private static final List<BigDecimal> NUMS_2_3_4 = List.of(
            new BigDecimal("2"),
            new BigDecimal("3"),
            new BigDecimal("4")
    );

    private static ExpressionEnvironment env(String name, Object value) {
        return ExpressionEnvironment.builder()
                .registerExternalSymbol(name, value, true)
                .build();
    }

    private static ExpressionEnvironment numsEnv(List<BigDecimal> nums) {
        return env("nums", nums);
    }

    // -------------------------------------------------------------------------
    // ..sum() — no transform (regression)
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("..sum() without transform")
    class SumNoTransform {

        @Test
        @DisplayName("nums..sum() = 6 for [1, 2, 3]")
        void sumOfVariable() {
            BigDecimal result = MathExpression.compile("nums..sum()", numsEnv(NUMS_1_2_3))
                    .compute(Map.of("nums", NUMS_1_2_3));
            assertThat(result).isEqualByComparingTo("6");
        }

        @Test
        @DisplayName("empty list..sum() = 0 (additive identity)")
        void sumOfEmptyList() {
            List<BigDecimal> empty = List.of();
            BigDecimal result = MathExpression.compile("nums..sum()", env("nums", empty))
                    .compute(Map.of("nums", empty));
            assertThat(result).isEqualByComparingTo("0");
        }
    }

    // -------------------------------------------------------------------------
    // ..sum(@ -> expr)
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("..sum(@ -> expr) with lambda transform")
    class SumWithTransform {

        @Test
        @DisplayName("nums..sum(@ -> @ * 2) = 12 for [1, 2, 3]")
        void sumDoubleEach() {
            BigDecimal result = MathExpression.compile("nums..sum(@ -> @ * 2)", numsEnv(NUMS_1_2_3))
                    .compute(Map.of("nums", NUMS_1_2_3));
            assertThat(result).isEqualByComparingTo("12");
        }

        @Test
        @DisplayName("nums..sum(@ -> @ ^ 2) = 14 for [1, 2, 3]")
        void sumSquareEach() {
            BigDecimal result = MathExpression.compile("nums..sum(@ -> @ ^ 2)", numsEnv(NUMS_1_2_3))
                    .compute(Map.of("nums", NUMS_1_2_3));
            assertThat(result).isEqualByComparingTo("14");
        }

        @Test
        @DisplayName("nums..sum(@ -> @ * 2^@) = 34 for [1, 2, 3]  (1*2 + 2*4 + 3*8)")
        void sumElementTimesExpPow() {
            BigDecimal result = MathExpression.compile("nums..sum(@ -> @ * 2^@)", numsEnv(NUMS_1_2_3))
                    .compute(Map.of("nums", NUMS_1_2_3));
            assertThat(result).isEqualByComparingTo("34");
        }

        @Test
        @DisplayName("single element list..sum(@ -> @ * 2) = 10")
        void sumSingleElement() {
            List<BigDecimal> single = List.of(new BigDecimal("5"));
            BigDecimal result = MathExpression.compile("nums..sum(@ -> @ * 2)", env("nums", single))
                    .compute(Map.of("nums", single));
            assertThat(result).isEqualByComparingTo("10");
        }

        @Test
        @DisplayName("empty list..sum(@ -> @ * 2) = 0 (additive identity)")
        void sumEmptyListWithTransform() {
            List<BigDecimal> empty = List.of();
            BigDecimal result = MathExpression.compile("nums..sum(@ -> @ * 2)", env("nums", empty))
                    .compute(Map.of("nums", empty));
            assertThat(result).isEqualByComparingTo("0");
        }

        @Test
        @DisplayName("sum with external variable in transform body: [1,2,3]..sum(@ -> @ + offset) = 36")
        void sumWithExternalVariable() {
            ExpressionEnvironment e = ExpressionEnvironment.builder()
                    .registerExternalSymbol("nums", NUMS_1_2_3, true)
                    .registerExternalSymbol("offset", new BigDecimal("10"), true)
                    .build();
            BigDecimal result = MathExpression.compile("nums..sum(@ -> @ + offset)", e)
                    .compute(Map.of("nums", NUMS_1_2_3, "offset", new BigDecimal("10")));
            assertThat(result).isEqualByComparingTo("36");
        }
    }

    // -------------------------------------------------------------------------
    // ..prod() — no transform
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("..prod() without transform")
    class ProdNoTransform {

        @Test
        @DisplayName("nums..prod() = 6 for [1, 2, 3]")
        void prodOf123() {
            BigDecimal result = MathExpression.compile("nums..prod()", numsEnv(NUMS_1_2_3))
                    .compute(Map.of("nums", NUMS_1_2_3));
            assertThat(result).isEqualByComparingTo("6");
        }

        @Test
        @DisplayName("nums..prod() = 24 for [2, 3, 4]")
        void prodOf234() {
            BigDecimal result = MathExpression.compile("nums..prod()", numsEnv(NUMS_2_3_4))
                    .compute(Map.of("nums", NUMS_2_3_4));
            assertThat(result).isEqualByComparingTo("24");
        }

        @Test
        @DisplayName("empty list..prod() = 1 (multiplicative identity)")
        void prodOfEmptyList() {
            List<BigDecimal> empty = List.of();
            BigDecimal result = MathExpression.compile("nums..prod()", env("nums", empty))
                    .compute(Map.of("nums", empty));
            assertThat(result).isEqualByComparingTo("1");
        }

        @Test
        @DisplayName("single element list..prod() = 5")
        void prodSingleElement() {
            List<BigDecimal> single = List.of(new BigDecimal("5"));
            BigDecimal result = MathExpression.compile("nums..prod()", env("nums", single))
                    .compute(Map.of("nums", single));
            assertThat(result).isEqualByComparingTo("5");
        }
    }

    // -------------------------------------------------------------------------
    // ..prod(@ -> expr)
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("..prod(@ -> expr) with lambda transform")
    class ProdWithTransform {

        @Test
        @DisplayName("nums..prod(@ -> @ * 2) = 48 for [1, 2, 3]  (2 * 4 * 6)")
        void prodDoubleEach() {
            BigDecimal result = MathExpression.compile("nums..prod(@ -> @ * 2)", numsEnv(NUMS_1_2_3))
                    .compute(Map.of("nums", NUMS_1_2_3));
            assertThat(result).isEqualByComparingTo("48");
        }

        @Test
        @DisplayName("empty list..prod(@ -> @ * 2) = 1 (multiplicative identity)")
        void prodEmptyListWithTransform() {
            List<BigDecimal> empty = List.of();
            BigDecimal result = MathExpression.compile("nums..prod(@ -> @ * 2)", env("nums", empty))
                    .compute(Map.of("nums", empty));
            assertThat(result).isEqualByComparingTo("1");
        }
    }

    // -------------------------------------------------------------------------
    // Invalid usages
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Invalid usages are rejected at compile time")
    class InvalidUsages {

        @Test
        @DisplayName("..sum(1 + 2) — positional args rejected for sum")
        void sumPositionalArgRejected() {
            assertThatThrownBy(() -> MathExpression.compile("nums..sum(1 + 2)", numsEnv(NUMS_1_2_3)))
                    .isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("..prod(1 + 2) — positional args rejected for prod")
        void prodPositionalArgRejected() {
            assertThatThrownBy(() -> MathExpression.compile("nums..prod(1 + 2)", numsEnv(NUMS_1_2_3)))
                    .isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("..avg(@ -> @ * 2) — lambda rejected for avg")
        void avgLambdaRejected() {
            assertThatThrownBy(() -> MathExpression.compile("nums..avg(@ -> @ * 2)", numsEnv(NUMS_1_2_3)))
                    .isInstanceOf(Exception.class);
        }
    }
}
