package com.runestone.expeval.api;

import com.runestone.expeval.environment.ExpressionEnvironment;
import org.assertj.core.api.Assertions;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static java.math.BigDecimal.ONE;
import static org.assertj.core.api.Assertions.assertThat;

// ComparableFunctions.max(T[]) and min(T[]) exercise the VectorValue → T[] coercion path
// in RuntimeCoercionService. Vector literals [a, b, c] are coerced to Comparable[] via the
// component type, which resolves to BigDecimal for numeric elements
// (Comparable.isInstance(BigDecimal) = true). See runtime-internals.md §5.
//
// Empty vector literal [] is not valid grammar (vectorOfEntitiesOperation requires ≥ 1 element),
// so the empty-array boundary from ComparableFunctionsTest cannot be reproduced via the API.
@DisplayName("ComparableFunctions via MathExpression/LogicalExpression API")
class ComparableFunctionsExpressionTest {

    private static final Offset<BigDecimal> EPSILON = Assertions.within(new BigDecimal("0.00000000000000000001"));
    private static final ExpressionEnvironment ENV = ExpressionEnvironment.builder().addComparableFunctions().build();

    @Nested
    @DisplayName("max — maximum of a numeric vector")
    class Max {

        @Test
        @DisplayName("max of two elements returns the larger one")
        void maxOfTwoElements() {
            assertThat(MathExpression.compile("max([10, 20])", ENV).compute())
                    .isCloseTo(new BigDecimal("20"), EPSILON);
        }

        @Test
        @DisplayName("max of three elements returns the largest")
        void maxOfThreeElements() {
            assertThat(MathExpression.compile("max([10, 30, 20])", ENV).compute())
                    .isCloseTo(new BigDecimal("30"), EPSILON);
        }

        @Test
        @DisplayName("max of single element returns that element")
        void maxOfSingleElement() {
            assertThat(MathExpression.compile("max([42])", ENV).compute())
                    .isCloseTo(new BigDecimal("42"), EPSILON);
        }

        @Test
        @DisplayName("max of all equal elements returns the common value")
        void maxOfAllEqual() {
            assertThat(MathExpression.compile("max([5, 5, 5])", ENV).compute())
                    .isCloseTo(new BigDecimal("5"), EPSILON);
        }

        @Test
        @DisplayName("max with negative numbers returns the least negative")
        void maxWithNegativeNumbers() {
            assertThat(MathExpression.compile("max([-1, -3, -2])", ENV).compute())
                    .isCloseTo(new BigDecimal("-1"), EPSILON);
        }

        @Test
        @DisplayName("max via variable vector bound at evaluation time")
        void maxViaVariable() {
            BigDecimal result = MathExpression.compile("max(nums)", ENV)
                    .compute(Map.of("nums", List.of(
                            new BigDecimal("7"),
                            new BigDecimal("3"),
                            new BigDecimal("9"),
                            new BigDecimal("1"))));
            assertThat(result).isCloseTo(new BigDecimal("9"), EPSILON);
        }

        @Test
        @DisplayName("max used in arithmetic: max([1, 2, 3]) + 10 = 13")
        void maxInArithmeticExpression() {
            assertThat(MathExpression.compile("max([1, 2, 3]) + 10", ENV).compute())
                    .isCloseTo(new BigDecimal("13"), EPSILON);
        }

        @Test
        @DisplayName("max in logical comparison: max([1, 2, 3]) > 2 → true")
        void maxInLogicalExpression() {
            boolean result = LogicalExpression.compile("max([1, 2, 3]) > 2", ENV).compute();
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("max in logical comparison: max([1, 2, 3]) > 5 → false")
        void maxBelowThresholdInLogicalExpression() {
            boolean result = LogicalExpression.compile("max([1, 2, 3]) > 5", ENV).compute();
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("min — minimum of a numeric vector")
    class Min {

        @Test
        @DisplayName("min of two elements returns the smaller one")
        void minOfTwoElements() {
            assertThat(MathExpression.compile("min([10, 20])", ENV).compute())
                    .isCloseTo(new BigDecimal("10"), EPSILON);
        }

        @Test
        @DisplayName("min of three elements returns the smallest")
        void minOfThreeElements() {
            assertThat(MathExpression.compile("min([30, 10, 20])", ENV).compute())
                    .isCloseTo(new BigDecimal("10"), EPSILON);
        }

        @Test
        @DisplayName("min of single element returns that element")
        void minOfSingleElement() {
            assertThat(MathExpression.compile("min([99])", ENV).compute())
                    .isCloseTo(new BigDecimal("99"), EPSILON);
        }

        @Test
        @DisplayName("min of all equal elements returns the common value")
        void minOfAllEqual() {
            assertThat(MathExpression.compile("min([7, 7, 7])", ENV).compute())
                    .isCloseTo(new BigDecimal("7"), EPSILON);
        }

        @Test
        @DisplayName("min with negative numbers returns the most negative")
        void minWithNegativeNumbers() {
            assertThat(MathExpression.compile("min([-1, -3, -2])", ENV).compute())
                    .isCloseTo(new BigDecimal("-3"), EPSILON);
        }

        @Test
        @DisplayName("min via variable vector bound at evaluation time")
        void minViaVariable() {
            BigDecimal result = MathExpression.compile("min(nums)", ENV)
                    .compute(Map.of("nums", List.of(
                            new BigDecimal("7"),
                            new BigDecimal("3"),
                            new BigDecimal("9"),
                            new BigDecimal("1"))));
            assertThat(result).isCloseTo(ONE, EPSILON);
        }

        @Test
        @DisplayName("min used in arithmetic: min([1, 2, 3]) + 10 = 11")
        void minInArithmeticExpression() {
            assertThat(MathExpression.compile("min([1, 2, 3]) + 10", ENV).compute())
                    .isCloseTo(new BigDecimal("11"), EPSILON);
        }

        @Test
        @DisplayName("min in logical comparison: min([1, 2, 3]) < 2 → true")
        void minInLogicalExpression() {
            boolean result = LogicalExpression.compile("min([1, 2, 3]) < 2", ENV).compute();
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("max and min combined")
    class MaxAndMin {

        @Test
        @DisplayName("max([a, b, c]) - min([a, b, c]) = range")
        void rangeAsMaxMinusMins() {
            BigDecimal result = MathExpression.compile("max([10, 30, 20]) - min([10, 30, 20])", ENV).compute();
            assertThat(result).isCloseTo(new BigDecimal("20"), EPSILON);
        }

        @Test
        @DisplayName("max and min on same vector of variables")
        void maxAndMinOnVariables() {
            BigDecimal maxResult = MathExpression.compile("max(nums)", ENV)
                    .compute(Map.of("nums", List.of(new BigDecimal("5"), new BigDecimal("1"), new BigDecimal("9"))));
            BigDecimal minResult = MathExpression.compile("min(nums)", ENV)
                    .compute(Map.of("nums", List.of(new BigDecimal("5"), new BigDecimal("1"), new BigDecimal("9"))));
            assertThat(maxResult).isCloseTo(new BigDecimal("9"), EPSILON);
            assertThat(minResult).isCloseTo(ONE, EPSILON);
        }

        @Test
        @DisplayName("max > min for distinct values → true")
        void maxAlwaysGreaterThanMin() {
            boolean result = LogicalExpression.compile("max([3, 1, 2]) > min([3, 1, 2])", ENV).compute();
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("max and min with MathFunctions")
    class WithMathFunctions {

        private static final ExpressionEnvironment MATH_ENV = ExpressionEnvironment.builder()
                .addComparableFunctions()
                .addMathFunctions()
                .build();

        @Test
        @DisplayName("max of logarithm results: max([ln(1), ln(e^2)]) = 2")
        void maxOfLogResults() {
            // ln(1)=0, ln(e^2)=2 — we approximate e^2 ≈ 7.389056...
            BigDecimal result = MathExpression.compile("max([ln(1), ln(7.38905609893065)])", MATH_ENV).compute();
            assertThat(result).isCloseTo(new BigDecimal("2"), Assertions.within(new BigDecimal("0.0001")));
        }

        @Test
        @DisplayName("min of rule3d results across scenarios")
        void minOfRule3dResults() {
            // rule3d(100, 20, 50)=10, rule3d(100, 20, 25)=5 → min = 5
            BigDecimal result = MathExpression.compile("min([rule3d(100, 20, 50), rule3d(100, 20, 25)])", MATH_ENV).compute();
            assertThat(result).isCloseTo(new BigDecimal("5"), EPSILON);
        }
    }
}
