package com.runestone.expeval2.api;

import com.runestone.expeval2.environment.ExpressionEnvironment;
import org.assertj.core.api.Assertions;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static org.assertj.core.api.Assertions.assertThat;

// Array-parameter functions (mean, geometricMean, harmonicMean, variance, stdDev, meanDev,
// distribute, spread) cannot be invoked via the expression API due to the VectorValue → T[]
// coercion gap in RuntimeCoercionService. Only scalar-parameter functions are tested here.
// See runtime-internals.md §5 for the root cause and the proposed fix path.
@DisplayName("MathFunctions scalar functions via MathExpression/LogicalExpression API")
class MathFunctionsExpressionTest {

    private static final Offset<BigDecimal> EPSILON = Assertions.within(new BigDecimal("0.00000000000000000001"));
    private static final ExpressionEnvironment ENV = ExpressionEnvironment.builder().addMathFunctions().build();

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
                    .setValue("x", ONE)
                    .compute();
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
                    .setValue("x", new BigDecimal("4"))
                    .compute();
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
                    .setValue("base", new BigDecimal("10"))
                    .setValue("value", new BigDecimal("1000"))
                    .compute();
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
                    .setValue("x", new BigDecimal("42"))
                    .compute();
            assertThat(result).isCloseTo(new BigDecimal("42"), EPSILON);
        }

        @Test
        @DisplayName("rule3d via variables: rule3d(a, b, c)")
        void directRuleViaVariables() {
            BigDecimal result = MathExpression.compile("rule3d(a, b, c)", ENV)
                    .setValue("a", new BigDecimal("200"))
                    .setValue("b", new BigDecimal("50"))
                    .setValue("c", new BigDecimal("100"))
                    .compute();
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
                    .setValue("x", new BigDecimal("100"))
                    .compute();
            assertThat(result).isCloseTo(new BigDecimal("20"), EPSILON);
        }

        @Test
        @DisplayName("rule3i via variables: rule3i(a, b, c)")
        void inverseRuleViaVariables() {
            BigDecimal result = MathExpression.compile("rule3i(a, b, c)", ENV)
                    .setValue("a", new BigDecimal("100"))
                    .setValue("b", new BigDecimal("30"))
                    .setValue("c", new BigDecimal("150"))
                    .compute();
            assertThat(result).isCloseTo(new BigDecimal("20"), EPSILON);
        }

        @Test
        @DisplayName("rule3i result in logical comparison: rule3i(100, 20, 50) > 20 → true")
        void rule3iInLogicalExpression() {
            boolean result = LogicalExpression.compile("rule3i(100, 20, 50) > 20", ENV).compute();
            assertThat(result).isTrue();
        }
    }
}
