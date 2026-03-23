package com.runestone.expeval2.api;

import com.runestone.expeval2.environment.ExpressionEnvironment;
import org.assertj.core.api.Assertions;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TrigonometryFunctions via MathExpression/LogicalExpression API")
class TrigonometryFunctionsExpressionTest {

    private static final Offset<BigDecimal> EPSILON = Assertions.within(new BigDecimal("0.00000000000000000001"));
    private static final String PI_HALF = "1.57079632679489661923";
    private static final ExpressionEnvironment ENV = ExpressionEnvironment.builder().addTrigonometryFunctions().build();

    @Test
    @DisplayName("sin(0) = 0")
    void sinOfZero() {
        assertThat(MathExpression.compile("sin(0)", ENV).compute()).isCloseTo(ZERO, EPSILON);
    }

    @Test
    @DisplayName("sin(π/2) = 1")
    void sinOfPiHalf() {
        assertThat(MathExpression.compile("sin(" + PI_HALF + ")", ENV).compute()).isCloseTo(ONE, EPSILON);
    }

    @Test
    @DisplayName("cos(0) = 1")
    void cosOfZero() {
        assertThat(MathExpression.compile("cos(0)", ENV).compute()).isCloseTo(ONE, EPSILON);
    }

    @Test
    @DisplayName("cos(π/2) = 0")
    void cosOfPiHalf() {
        assertThat(MathExpression.compile("cos(" + PI_HALF + ")", ENV).compute()).isCloseTo(ZERO, EPSILON);
    }

    @Test
    @DisplayName("tan(0) = 0")
    void tanOfZero() {
        assertThat(MathExpression.compile("tan(0)", ENV).compute()).isCloseTo(ZERO, EPSILON);
    }

    @Test
    @DisplayName("asin(0) = 0")
    void asinOfZero() {
        assertThat(MathExpression.compile("asin(0)", ENV).compute()).isCloseTo(ZERO, EPSILON);
    }

    @Test
    @DisplayName("asin(1) = π/2")
    void asinOfOne() {
        assertThat(MathExpression.compile("asin(1)", ENV).compute())
                .isCloseTo(new BigDecimal(PI_HALF), EPSILON);
    }

    @Test
    @DisplayName("acos(1) = 0")
    void acosOfOne() {
        assertThat(MathExpression.compile("acos(1)", ENV).compute()).isCloseTo(ZERO, EPSILON);
    }

    @Test
    @DisplayName("acos(0) = π/2")
    void acosOfZero() {
        assertThat(MathExpression.compile("acos(0)", ENV).compute())
                .isCloseTo(new BigDecimal(PI_HALF), EPSILON);
    }

    @Test
    @DisplayName("atan(0) = 0")
    void atanOfZero() {
        assertThat(MathExpression.compile("atan(0)", ENV).compute()).isCloseTo(ZERO, EPSILON);
    }

    @Test
    @DisplayName("atan2(0, 1) = 0")
    void atan2ZeroOverOne() {
        assertThat(MathExpression.compile("atan2(0, 1)", ENV).compute()).isCloseTo(ZERO, EPSILON);
    }

    @Test
    @DisplayName("atan2(1, 0) = π/2")
    void atan2OneOverZero() {
        assertThat(MathExpression.compile("atan2(1, 0)", ENV).compute())
                .isCloseTo(new BigDecimal(PI_HALF), EPSILON);
    }

    @Test
    @DisplayName("sinh(0) = 0")
    void sinhOfZero() {
        assertThat(MathExpression.compile("sinh(0)", ENV).compute()).isCloseTo(ZERO, EPSILON);
    }

    @Test
    @DisplayName("cosh(0) = 1")
    void coshOfZero() {
        assertThat(MathExpression.compile("cosh(0)", ENV).compute()).isCloseTo(ONE, EPSILON);
    }

    @Test
    @DisplayName("tanh(0) = 0")
    void tanhOfZero() {
        assertThat(MathExpression.compile("tanh(0)", ENV).compute()).isCloseTo(ZERO, EPSILON);
    }

    @Test
    @DisplayName("asinh(0) = 0")
    void asinhOfZero() {
        assertThat(MathExpression.compile("asinh(0)", ENV).compute()).isCloseTo(ZERO, EPSILON);
    }

    @Test
    @DisplayName("acosh(1) = 0")
    void acoshOfOne() {
        assertThat(MathExpression.compile("acosh(1)", ENV).compute()).isCloseTo(ZERO, EPSILON);
    }

    @Test
    @DisplayName("atanh(0) = 0")
    void atanhOfZero() {
        assertThat(MathExpression.compile("atanh(0)", ENV).compute()).isCloseTo(ZERO, EPSILON);
    }

    @Test
    @DisplayName("sin via variable argument")
    void sinViaVariable() {
        BigDecimal result = MathExpression.compile("sin(x)", ENV)
                .compute(Map.of("x", ZERO));
        assertThat(result).isCloseTo(ZERO, EPSILON);
    }

    @Test
    @DisplayName("atan2 result used in logical comparison: atan2(1,0) > 1 → true")
    void atan2InLogicalExpression() {
        boolean result = LogicalExpression.compile("atan2(1, 0) > 1", ENV).compute();
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("sin² + cos² = 1 (Pythagorean identity)")
    void pythagoreanIdentity() {
        BigDecimal result = MathExpression.compile("sin(x) * sin(x) + cos(x) * cos(x)", ENV)
                .compute(Map.of("x", new BigDecimal("0.7853981633974483")));
        assertThat(result).isCloseTo(ONE, Assertions.within(new BigDecimal("0.00000001")));
    }
}
