package com.runestone.expeval.catalog.functions;

import org.assertj.core.api.Assertions;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.MathContext;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TrigonometryFunctions Tests")
class TrigonometryFunctionsTest {

    private static final MathContext MC = MathContext.DECIMAL128;
    private static final Offset<BigDecimal> EPSILON = Assertions.within(new BigDecimal("0.00000000000000000001"));

    @Test
    @DisplayName("sin()")
    void sinTest() {
        assertThat(TrigonometryFunctions.sin(MC, ZERO)).isCloseTo(ZERO, EPSILON);
        assertThat(TrigonometryFunctions.sin(MC, new BigDecimal("1.57079632679489661923"))).isCloseTo(ONE, EPSILON);
    }

    @Test
    @DisplayName("cos()")
    void cosTest() {
        assertThat(TrigonometryFunctions.cos(MC, ZERO)).isCloseTo(ONE, EPSILON);
        assertThat(TrigonometryFunctions.cos(MC, new BigDecimal("1.57079632679489661923"))).isCloseTo(ZERO, EPSILON);
    }

    @Test
    @DisplayName("tan()")
    void tanTest() {
        assertThat(TrigonometryFunctions.tan(MC, ZERO)).isCloseTo(ZERO, EPSILON);
    }

    @Test
    @DisplayName("asin()")
    void asinTest() {
        assertThat(TrigonometryFunctions.asin(MC, ZERO)).isCloseTo(ZERO, EPSILON);
        assertThat(TrigonometryFunctions.asin(MC, ONE)).isCloseTo(new BigDecimal("1.57079632679489661923"), EPSILON);
    }

    @Test
    @DisplayName("acos()")
    void acosTest() {
        assertThat(TrigonometryFunctions.acos(MC, ONE)).isCloseTo(ZERO, EPSILON);
        assertThat(TrigonometryFunctions.acos(MC, ZERO)).isCloseTo(new BigDecimal("1.57079632679489661923"), EPSILON);
    }

    @Test
    @DisplayName("atan()")
    void atanTest() {
        assertThat(TrigonometryFunctions.atan(MC, ZERO)).isCloseTo(ZERO, EPSILON);
    }

    @Test
    @DisplayName("atan2()")
    void atan2Test() {
        assertThat(TrigonometryFunctions.atan2(MC, ZERO, ONE)).isCloseTo(ZERO, EPSILON);
        assertThat(TrigonometryFunctions.atan2(MC, ONE, ZERO)).isCloseTo(new BigDecimal("1.57079632679489661923"), EPSILON);
    }

    @Test
    @DisplayName("sinh()")
    void sinhTest() {
        assertThat(TrigonometryFunctions.sinh(MC, ZERO)).isCloseTo(ZERO, EPSILON);
    }

    @Test
    @DisplayName("cosh()")
    void coshTest() {
        assertThat(TrigonometryFunctions.cosh(MC, ZERO)).isCloseTo(ONE, EPSILON);
    }

    @Test
    @DisplayName("tanh()")
    void tanhTest() {
        assertThat(TrigonometryFunctions.tanh(MC, ZERO)).isCloseTo(ZERO, EPSILON);
    }

    @Test
    @DisplayName("asinh()")
    void asinhTest() {
        assertThat(TrigonometryFunctions.asinh(MC, ZERO)).isCloseTo(ZERO, EPSILON);
    }

    @Test
    @DisplayName("acosh()")
    void acoshTest() {
        assertThat(TrigonometryFunctions.acosh(MC, ONE)).isCloseTo(ZERO, EPSILON);
    }

    @Test
    @DisplayName("atanh()")
    void atanhTest() {
        assertThat(TrigonometryFunctions.atanh(MC, ZERO)).isCloseTo(ZERO, EPSILON);
    }
}
