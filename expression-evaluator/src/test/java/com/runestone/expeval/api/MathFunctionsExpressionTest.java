package com.runestone.expeval.api;

import com.runestone.expeval.environment.ExpressionEnvironment;
import org.assertj.core.api.Assertions;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MathFunctions via MathExpression/LogicalExpression API")
class MathFunctionsExpressionTest {

    private static final Offset<BigDecimal> EPSILON = Assertions.within(new BigDecimal("0.00000000000000000001"));
    private static final ExpressionEnvironment ENV = ExpressionEnvironment.builder().addMathFunctions().build();

    @Nested
    @DisplayName("mean — arithmetic mean of a numeric vector")
    class Mean {

        @Test
        @DisplayName("mean of literal vector returns arithmetic average")
        void meanOfLiteralVector() {
            assertThat(MathExpression.compile("mean([1, 2, 3, 4])", ENV).compute())
                    .isCloseTo(new BigDecimal("2.5"), EPSILON);
        }

        @Test
        @DisplayName("mean via variable vector bound at evaluation time")
        void meanViaVariableVector() {
            BigDecimal result = MathExpression.compile("mean(nums)", ENV)
                    .compute(Map.of("nums", java.util.List.of(
                            new BigDecimal("10"),
                            new BigDecimal("20"),
                            new BigDecimal("30"))));
            assertThat(result).isCloseTo(new BigDecimal("20"), EPSILON);
        }

        @Test
        @DisplayName("mean used in logical comparison")
        void meanInLogicalExpression() {
            boolean result = LogicalExpression.compile("mean([2, 4, 6]) = 4", ENV).compute();
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("ln — natural logarithm")
    class Ln {

        @Test
        @DisplayName("ln(1) = 0")
        void lnOfOne() {
            assertThat(MathExpression.compile("ln(1)", ENV).compute()).isCloseTo(ZERO, EPSILON);
        }

        @Test
        @DisplayName("ln(x) via variable: ln(1) = 0")
        void lnViaVariable() {
            BigDecimal result = MathExpression.compile("ln(x)", ENV)
                    .compute(Map.of("x", ONE));
            assertThat(result).isCloseTo(ZERO, EPSILON);
        }

        @Test
        @DisplayName("ln used in a composed expression: ln(1) + 5 = 5")
        void lnComposedInArithmeticExpression() {
            assertThat(MathExpression.compile("ln(1) + 5", ENV).compute())
                    .isCloseTo(new BigDecimal("5"), EPSILON);
        }

        @Test
        @DisplayName("ln result in logical comparison: ln(1) == 0 → true")
        void lnInLogicalExpression() {
            boolean result = LogicalExpression.compile("ln(1) = 0", ENV).compute();
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("lb — binary logarithm (base 2)")
    class Lb {

        @Test
        @DisplayName("lb(1) = 0")
        void lbOfOne() {
            assertThat(MathExpression.compile("lb(1)", ENV).compute()).isCloseTo(ZERO, EPSILON);
        }

        @Test
        @DisplayName("lb(2) = 1")
        void lbOfTwo() {
            assertThat(MathExpression.compile("lb(2)", ENV).compute()).isCloseTo(ONE, EPSILON);
        }

        @Test
        @DisplayName("lb(8) = 3")
        void lbOfEight() {
            assertThat(MathExpression.compile("lb(8)", ENV).compute())
                    .isCloseTo(new BigDecimal("3"), EPSILON);
        }

        @Test
        @DisplayName("lb via variable: lb(x) with x=4 → 2")
        void lbViaVariable() {
            BigDecimal result = MathExpression.compile("lb(x)", ENV)
                    .compute(Map.of("x", new BigDecimal("4")));
            assertThat(result).isCloseTo(new BigDecimal("2"), EPSILON);
        }

        @Test
        @DisplayName("lb result in logical comparison: lb(8) > 2 → true")
        void lbInLogicalExpression() {
            boolean result = LogicalExpression.compile("lb(8) > 2", ENV).compute();
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("log — logarithm with custom base")
    class Log {

        @Test
        @DisplayName("log(10, 100) = 2")
        void logBase10Of100() {
            assertThat(MathExpression.compile("log(10, 100)", ENV).compute())
                    .isCloseTo(new BigDecimal("2"), EPSILON);
        }

        @Test
        @DisplayName("log(2, 8) = 3")
        void logBase2Of8() {
            assertThat(MathExpression.compile("log(2, 8)", ENV).compute())
                    .isCloseTo(new BigDecimal("3"), EPSILON);
        }

        @Test
        @DisplayName("log(base, value) via variables: log(10, 1000) = 3")
        void logViaVariables() {
            BigDecimal result = MathExpression.compile("log(base, value)", ENV)
                    .compute(Map.of("base", new BigDecimal("10"), "value", new BigDecimal("1000")));
            assertThat(result).isCloseTo(new BigDecimal("3"), EPSILON);
        }

        @Test
        @DisplayName("log result in logical comparison: log(10, 100) = 2 → true")
        void logInLogicalExpression() {
            boolean result = LogicalExpression.compile("log(10, 100) = 2", ENV).compute();
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("rule3d — direct rule of three")
    class Rule3d {

        @Test
        @DisplayName("rule3d(100, 20, 50) = 10 — proportional scale down")
        void directRuleScalesProportionally() {
            assertThat(MathExpression.compile("rule3d(100, 20, 50)", ENV).compute())
                    .isCloseTo(new BigDecimal("10"), EPSILON);
        }

        @Test
        @DisplayName("rule3d(1, 1, x) = x — identity when origin equals result")
        void directRuleIdentityWhenOriginEqualsResult() {
            BigDecimal result = MathExpression.compile("rule3d(1, 1, x)", ENV)
                    .compute(Map.of("x", new BigDecimal("42")));
            assertThat(result).isCloseTo(new BigDecimal("42"), EPSILON);
        }

        @Test
        @DisplayName("rule3d via variables: rule3d(a, b, c)")
        void directRuleViaVariables() {
            BigDecimal result = MathExpression.compile("rule3d(a, b, c)", ENV)
                    .compute(Map.of("a", new BigDecimal("200"), "b", new BigDecimal("50"), "c", new BigDecimal("100")));
            assertThat(result).isCloseTo(new BigDecimal("25"), EPSILON);
        }

        @Test
        @DisplayName("rule3d result in logical comparison: rule3d(100, 20, 50) < 20 → true")
        void rule3dInLogicalExpression() {
            boolean result = LogicalExpression.compile("rule3d(100, 20, 50) < 20", ENV).compute();
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("rule3i — inverse rule of three")
    class Rule3i {

        @Test
        @DisplayName("rule3i(100, 20, 50) = 40 — inverting origin halves the result")
        void inverseRuleInverselyProportional() {
            assertThat(MathExpression.compile("rule3i(100, 20, 50)", ENV).compute())
                    .isCloseTo(new BigDecimal("40"), EPSILON);
        }

        @Test
        @DisplayName("rule3i(x, 20, x) = 20 — same origin1 and origin2 yields same result")
        void inverseRuleSameOrigins() {
            BigDecimal result = MathExpression.compile("rule3i(x, 20, x)", ENV)
                    .compute(Map.of("x", new BigDecimal("100")));
            assertThat(result).isCloseTo(new BigDecimal("20"), EPSILON);
        }

        @Test
        @DisplayName("rule3i via variables: rule3i(a, b, c)")
        void inverseRuleViaVariables() {
            BigDecimal result = MathExpression.compile("rule3i(a, b, c)", ENV)
                    .compute(Map.of("a", new BigDecimal("100"), "b", new BigDecimal("30"), "c", new BigDecimal("150")));
            assertThat(result).isCloseTo(new BigDecimal("20"), EPSILON);
        }

        @Test
        @DisplayName("rule3i result in logical comparison: rule3i(100, 20, 50) > 20 → true")
        void rule3iInLogicalExpression() {
            boolean result = LogicalExpression.compile("rule3i(100, 20, 50) > 20", ENV).compute();
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("geometricMean — nth root of the product of all values")
    class GeometricMean {

        @Test
        @DisplayName("geometricMean([2, 8]) = 4 — square root of product")
        void geometricMeanOfLiteralVector() {
            assertThat(MathExpression.compile("geometricMean([2, 8])", ENV).compute())
                    .isCloseTo(new BigDecimal("4"), EPSILON);
        }

        @Test
        @DisplayName("geometricMean via variable vector: geometricMean([4, 1, 0.03125]) = 0.5")
        void geometricMeanViaVariableVector() {
            BigDecimal result = MathExpression.compile("geometricMean(nums)", ENV)
                    .compute(Map.of("nums", java.util.List.of(
                            new BigDecimal("4"),
                            new BigDecimal("1"),
                            new BigDecimal("0.03125"))));
            assertThat(result).isCloseTo(new BigDecimal("0.5"), EPSILON);
        }

        @Test
        @DisplayName("geometricMean used in logical comparison: geometricMean([2, 8]) = 4 → true")
        void geometricMeanInLogicalExpression() {
            boolean result = LogicalExpression.compile("geometricMean([2, 8]) = 4", ENV).compute();
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("harmonicMean — reciprocal of the arithmetic mean of reciprocals")
    class HarmonicMean {

        @Test
        @DisplayName("harmonicMean([1, 4, 4]) = 2")
        void harmonicMeanOfLiteralVector() {
            assertThat(MathExpression.compile("harmonicMean([1, 4, 4])", ENV).compute())
                    .isCloseTo(new BigDecimal("2"), EPSILON);
        }

        @Test
        @DisplayName("harmonicMean via variable vector")
        void harmonicMeanViaVariableVector() {
            BigDecimal result = MathExpression.compile("harmonicMean(nums)", ENV)
                    .compute(Map.of("nums", java.util.List.of(
                            new BigDecimal("1"),
                            new BigDecimal("4"),
                            new BigDecimal("4"))));
            assertThat(result).isCloseTo(new BigDecimal("2"), EPSILON);
        }

        @Test
        @DisplayName("harmonicMean used in logical comparison: harmonicMean([1, 4, 4]) = 2 → true")
        void harmonicMeanInLogicalExpression() {
            boolean result = LogicalExpression.compile("harmonicMean([1, 4, 4]) = 2", ENV).compute();
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("meanDev — mean of absolute deviations from the mean")
    class MeanDev {

        @Test
        @DisplayName("meanDev([3, 6, 6, 7, 8, 11, 15, 16]) = 3.75")
        void meanDevOfLiteralVector() {
            // mean = 9; absolute deviations: 6,3,3,2,1,2,6,7 → sum 30 → 30/8 = 3.75
            assertThat(MathExpression.compile("meanDev([3, 6, 6, 7, 8, 11, 15, 16])", ENV).compute())
                    .isCloseTo(new BigDecimal("3.75"), EPSILON);
        }

        @Test
        @DisplayName("meanDev via variable vector")
        void meanDevViaVariableVector() {
            BigDecimal result = MathExpression.compile("meanDev(nums)", ENV)
                    .compute(Map.of("nums", java.util.List.of(
                            new BigDecimal("3"),
                            new BigDecimal("6"),
                            new BigDecimal("6"),
                            new BigDecimal("7"),
                            new BigDecimal("8"),
                            new BigDecimal("11"),
                            new BigDecimal("15"),
                            new BigDecimal("16"))));
            assertThat(result).isCloseTo(new BigDecimal("3.75"), EPSILON);
        }

        @Test
        @DisplayName("meanDev used in logical comparison: meanDev([3, 6, 6, 7, 8, 11, 15, 16]) > 3 → true")
        void meanDevInLogicalExpression() {
            boolean result = LogicalExpression.compile("meanDev([3, 6, 6, 7, 8, 11, 15, 16]) > 3", ENV).compute();
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("variance — population (type=0) and sample (type=1) variance")
    class Variance {

        @Test
        @DisplayName("population variance of [2,4,4,4,5,5,7,9] = 4")
        void populationVarianceOfLiteralVector() {
            // mean=5; squared deviations: 9,1,1,1,0,0,4,16 → sum 32 → 32/8 = 4
            assertThat(MathExpression.compile("variance([2, 4, 4, 4, 5, 5, 7, 9], 0)", ENV).compute())
                    .isCloseTo(new BigDecimal("4"), EPSILON);
        }

        @Test
        @DisplayName("sample variance of [2,4,4,4,5,5,7,9] = 32/7")
        void sampleVarianceOfLiteralVector() {
            // 32/7 ≈ 4.571428571428571428571428571428571 (DECIMAL128 precision)
            assertThat(MathExpression.compile("variance([2, 4, 4, 4, 5, 5, 7, 9], 1)", ENV).compute())
                    .isCloseTo(new BigDecimal("4.571428571428571428571428571428571"), EPSILON);
        }

        @Test
        @DisplayName("variance via variable vector")
        void varianceViaVariableVector() {
            BigDecimal result = MathExpression.compile("variance(nums, 0)", ENV)
                    .compute(Map.of("nums", java.util.List.of(
                            new BigDecimal("2"),
                            new BigDecimal("4"),
                            new BigDecimal("4"),
                            new BigDecimal("4"),
                            new BigDecimal("5"),
                            new BigDecimal("5"),
                            new BigDecimal("7"),
                            new BigDecimal("9"))));
            assertThat(result).isCloseTo(new BigDecimal("4"), EPSILON);
        }

        @Test
        @DisplayName("variance used in logical comparison: variance([2,4,4,4,5,5,7,9], 0) = 4 → true")
        void varianceInLogicalExpression() {
            boolean result = LogicalExpression.compile("variance([2, 4, 4, 4, 5, 5, 7, 9], 0) = 4", ENV).compute();
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("stdDev — population (type=0) and sample (type=1) standard deviation")
    class StdDev {

        @Test
        @DisplayName("population stdDev of [2,4,4,4,5,5,7,9] = 2 — exact square root")
        void populationStdDevOfLiteralVector() {
            // sqrt(variance=4) = 2
            assertThat(MathExpression.compile("stdDev([2, 4, 4, 4, 5, 5, 7, 9], 0)", ENV).compute())
                    .isCloseTo(new BigDecimal("2"), EPSILON);
        }

        @Test
        @DisplayName("sample stdDev of [2,4,4,4,5,5,7,9] = sqrt(32/7)")
        void sampleStdDevOfLiteralVector() {
            // sqrt(32/7) ≈ 2.138089935299395077476427847038028 (DECIMAL128 precision)
            assertThat(MathExpression.compile("stdDev([2, 4, 4, 4, 5, 5, 7, 9], 1)", ENV).compute())
                    .isCloseTo(new BigDecimal("2.138089935299395077476427847038028"), EPSILON);
        }

        @Test
        @DisplayName("stdDev via variable vector")
        void stdDevViaVariableVector() {
            BigDecimal result = MathExpression.compile("stdDev(nums, 0)", ENV)
                    .compute(Map.of("nums", java.util.List.of(
                            new BigDecimal("2"),
                            new BigDecimal("4"),
                            new BigDecimal("4"),
                            new BigDecimal("4"),
                            new BigDecimal("5"),
                            new BigDecimal("5"),
                            new BigDecimal("7"),
                            new BigDecimal("9"))));
            assertThat(result).isCloseTo(new BigDecimal("2"), EPSILON);
        }

        @Test
        @DisplayName("stdDev used in logical comparison: stdDev([2,4,4,4,5,5,7,9], 0) = 2 → true")
        void stdDevInLogicalExpression() {
            boolean result = LogicalExpression.compile("stdDev([2, 4, 4, 4, 5, 5, 7, 9], 0) = 2", ENV).compute();
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("distribute — sequential distribution with upper/lower limits per bucket")
    class Distribute {

        // distribute returns BigDecimal[] (VectorValue). To produce a scalar result
        // these tests compose distribute inside mean(), which validates the full
        // vector-returning function pipeline end-to-end through the expression API.

        @Test
        @DisplayName("mean(distribute(5, 1, [1,2,3], [4,4,4])) = 2.75 — forward distribution composed with mean")
        void distributeForwardComposedWithMean() {
            // distribute(5, dir≥0, [1,2,3], [4,4,4]) → [4,4,3,0] (remainder=0)
            // mean([4,4,3,0]) = 11/4 = 2.75
            assertThat(MathExpression.compile("mean(distribute(5, 1, [1, 2, 3], [4, 4, 4]))", ENV).compute())
                    .isCloseTo(new BigDecimal("2.75"), EPSILON);
        }

        @Test
        @DisplayName("distribute forward result compared in logical expression")
        void distributeInLogicalExpression() {
            // mean([4,4,3,0]) = 2.75 < 3
            boolean result = LogicalExpression.compile("mean(distribute(5, 1, [1, 2, 3], [4, 4, 4])) < 3", ENV).compute();
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("mean(distribute(5, -1, [1,2,3], [4,4,4])) = 2.75 — backward distribution same sum")
        void distributeBackwardComposedWithMean() {
            // distribute(5, dir<0, [1,2,3], [4,4,4]) → [3,4,4,0] (fills from last bucket)
            // mean([3,4,4,0]) = 11/4 = 2.75
            assertThat(MathExpression.compile("mean(distribute(5, -1, [1, 2, 3], [4, 4, 4]))", ENV).compute())
                    .isCloseTo(new BigDecimal("2.75"), EPSILON);
        }
    }

    @Nested
    @DisplayName("spread — proportional distribution of a value across reference weights")
    class Spread {

        // spread returns BigDecimal[] (VectorValue). Tests compose spread inside mean()
        // to produce a scalar, validating the vector-returning function pipeline.

        @Test
        @DisplayName("mean(spread(100, 1, [1, 3])) = 50 — parts 25 and 75 average to 50")
        void spreadProportionalComposedWithMean() {
            // spread(100, 1, [1,3]): total=4, parts=[25, 75]
            // mean([25, 75]) = 50
            assertThat(MathExpression.compile("mean(spread(100, 1, [1, 3]))", ENV).compute())
                    .isCloseTo(new BigDecimal("50"), EPSILON);
        }

        @Test
        @DisplayName("spread result compared in logical expression")
        void spreadInLogicalExpression() {
            // mean([25, 75]) = 50
            boolean result = LogicalExpression.compile("mean(spread(100, 1, [1, 3])) = 50", ENV).compute();
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("mean(spread(100, 1, [1, 1])) = 50 — equal references split evenly")
        void spreadEqualReferencesComposedWithMean() {
            // spread(100, 1, [1,1]): total=2, parts=[50, 50]
            // mean([50, 50]) = 50
            assertThat(MathExpression.compile("mean(spread(100, 1, [1, 1]))", ENV).compute())
                    .isCloseTo(new BigDecimal("50"), EPSILON);
        }
    }
}
