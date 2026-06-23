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

@DisplayName("..map(@ -> expr) — element-wise collection transform")
class VectorMapTransformTest {

    private static final List<BigDecimal> NUMS_1_2_3 = List.of(
            BigDecimal.ONE,
            new BigDecimal("2"),
            new BigDecimal("3")
    );

    private record Order(BigDecimal price, BigDecimal qty) {}

    private static final List<Order> ORDERS = List.of(
            new Order(new BigDecimal("5"), new BigDecimal("2")),
            new Order(new BigDecimal("3"), new BigDecimal("4"))
    );

    private static ExpressionEnvironment env(String name, Object value) {
        return ExpressionEnvironment.builder()
                .registerExternalSymbol(name, value, true)
                .build();
    }

    // -------------------------------------------------------------------------
    // Scalar list transforms
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Scalar list: ..map(@ -> expr)")
    class ScalarListMap {

        @Test
        @DisplayName("nums..map(@ -> @ * 2)..sum() = 12 for [1, 2, 3]")
        void mapDoubleAndSum() {
            BigDecimal result = MathExpression.compile("nums..map(@ -> @ * 2)..sum()", env("nums", NUMS_1_2_3))
                    .compute(Map.of("nums", NUMS_1_2_3));
            assertThat(result).isEqualByComparingTo("12");
        }

        @Test
        @DisplayName("nums..map(@ -> @ ^ 2)..sum() = 14 for [1, 2, 3]")
        void mapSquareAndSum() {
            BigDecimal result = MathExpression.compile("nums..map(@ -> @ ^ 2)..sum()", env("nums", NUMS_1_2_3))
                    .compute(Map.of("nums", NUMS_1_2_3));
            assertThat(result).isEqualByComparingTo("14");
        }

        @Test
        @DisplayName("nums..map(@ -> @ * 2)..avg() = 4 for [1, 2, 3]")
        void mapDoubleAndAvg() {
            BigDecimal result = MathExpression.compile("nums..map(@ -> @ * 2)..avg()", env("nums", NUMS_1_2_3))
                    .compute(Map.of("nums", NUMS_1_2_3));
            assertThat(result).isEqualByComparingTo("4");
        }

        @Test
        @DisplayName("nums..map(@ -> @ + 1)..count() = 3 for [1, 2, 3]")
        void mapAndCount() {
            BigDecimal result = MathExpression.compile("nums..map(@ -> @ + 1)..count()", env("nums", NUMS_1_2_3))
                    .compute(Map.of("nums", NUMS_1_2_3));
            assertThat(result).isEqualByComparingTo("3");
        }

        @Test
        @DisplayName("empty list..map()..sum() = 0 (additive identity)")
        void mapOnEmptyList() {
            List<BigDecimal> empty = List.of();
            BigDecimal result = MathExpression.compile("nums..map(@ -> @ * 2)..sum()", env("nums", empty))
                    .compute(Map.of("nums", empty));
            assertThat(result).isEqualByComparingTo("0");
        }

        @Test
        @DisplayName("chained maps: nums..map(@ -> @ * 2)..map(@ -> @ + 1)..sum()")
        void chainedMaps() {
            // [1,2,3] -> [2,4,6] -> [3,5,7] -> sum = 15
            BigDecimal result = MathExpression.compile(
                    "nums..map(@ -> @ * 2)..map(@ -> @ + 1)..sum()", env("nums", NUMS_1_2_3))
                    .compute(Map.of("nums", NUMS_1_2_3));
            assertThat(result).isEqualByComparingTo("15");
        }
    }

    // -------------------------------------------------------------------------
    // Object list transforms (the motivating example)
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Object list: ..map(@ -> @.prop) and ..map(@ -> @.a * @.b)")
    class ObjectListMap {

        @Test
        @DisplayName("orders..map(@ -> @.price)..sum() extracts and sums prices")
        void mapExtractPropertyAndSum() {
            // prices: 5 + 3 = 8
            ExpressionEnvironment env = env("orders", ORDERS);
            BigDecimal result = MathExpression.compile("orders..map(@ -> @.price)..sum()", env)
                    .compute(Map.of("orders", ORDERS));
            assertThat(result).isEqualByComparingTo("8");
        }

        @Test
        @DisplayName("orders..map(@ -> @.price * @.qty)..sum() = 5*2 + 3*4 = 22")
        void mapComputedFieldAndSum() {
            ExpressionEnvironment env = env("orders", ORDERS);
            BigDecimal result = MathExpression.compile("orders..map(@ -> @.price * @.qty)..sum()", env)
                    .compute(Map.of("orders", ORDERS));
            assertThat(result).isEqualByComparingTo("22");
        }

        @Test
        @DisplayName("orders..map(@ -> @.price * @.qty)..avg() = (10 + 12) / 2 = 11")
        void mapComputedFieldAndAvg() {
            ExpressionEnvironment env = env("orders", ORDERS);
            BigDecimal result = MathExpression.compile("orders..map(@ -> @.price * @.qty)..avg()", env)
                    .compute(Map.of("orders", ORDERS));
            assertThat(result).isEqualByComparingTo("11");
        }

        @Test
        @DisplayName("orders..map(@ -> @.price * @.qty)..max() = 12 (3*4)")
        void mapComputedFieldAndMax() {
            ExpressionEnvironment env = env("orders", ORDERS);
            BigDecimal result = MathExpression.compile("orders..map(@ -> @.price * @.qty)..max()", env)
                    .compute(Map.of("orders", ORDERS));
            assertThat(result).isEqualByComparingTo("12");
        }
    }

    // -------------------------------------------------------------------------
    // Map (key-value) transforms
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Map entry: ..map(@ -> @.value) and ..map(@ -> @.key + @.value)")
    class MapEntryTransform {

        @Test
        @DisplayName("mapObj..map(@ -> @.value * 2)..sum() = 2*(1+2+3) = 12")
        void mapValuesDoubleAndSum() {
            Map<String, BigDecimal> mapObj = Map.of(
                    "a", BigDecimal.ONE,
                    "b", new BigDecimal("2"),
                    "c", new BigDecimal("3")
            );
            ExpressionEnvironment env = env("mapObj", mapObj);
            BigDecimal result = MathExpression.compile("mapObj..map(@ -> @.value * 2)..sum()", env)
                    .compute(Map.of("mapObj", mapObj));
            assertThat(result).isEqualByComparingTo("12");
        }

        @Test
        @DisplayName("mapObj..map(@ -> @.value)..count() = number of map entries")
        void mapToValuesAndCount() {
            Map<String, BigDecimal> mapObj = Map.of(
                    "x", BigDecimal.ONE,
                    "y", new BigDecimal("2")
            );
            ExpressionEnvironment env = env("mapObj", mapObj);
            BigDecimal result = MathExpression.compile("mapObj..map(@ -> @.value)..count()", env)
                    .compute(Map.of("mapObj", mapObj));
            assertThat(result).isEqualByComparingTo("2");
        }

        @Test
        @DisplayName("mapObj..map(@ -> @.value)..sum() produces same result as ..values()..sum()")
        void mapValuesSumEquivalentToValuesSum() {
            Map<String, BigDecimal> mapObj = Map.of(
                    "p", new BigDecimal("10"),
                    "q", new BigDecimal("20")
            );
            ExpressionEnvironment env = env("mapObj", mapObj);
            BigDecimal viaMap = MathExpression.compile("mapObj..map(@ -> @.value)..sum()", env)
                    .compute(Map.of("mapObj", mapObj));
            BigDecimal viaValues = MathExpression.compile("mapObj..values()..sum()", env)
                    .compute(Map.of("mapObj", mapObj));
            assertThat(viaMap).isEqualByComparingTo(viaValues);
        }
    }

    // -------------------------------------------------------------------------
    // Error cases
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Error: invalid ..map() usage")
    class ErrorCases {

        @Test
        @DisplayName("..map() without lambda throws IllegalArgumentException")
        void mapWithoutLambdaThrows() {
            ExpressionEnvironment env = env("nums", NUMS_1_2_3);
            assertThatThrownBy(() -> MathExpression.compile("nums..map()..sum()", env))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("..map() requires a lambda transform");
        }

        @Test
        @DisplayName("..map(positionalArg) without lambda arrow throws IllegalArgumentException")
        void mapWithPositionalArgThrows() {
            ExpressionEnvironment env = env("nums", NUMS_1_2_3);
            assertThatThrownBy(() -> MathExpression.compile("nums..map(5)..sum()", env))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("..map() requires a lambda transform");
        }
    }
}
