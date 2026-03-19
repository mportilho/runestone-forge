package com.runestone.expeval2.catalog.functions;

import org.assertj.core.api.Assertions;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TrigonometryFunctions Tests")
class TrigonometryFunctionsTest {

    private static final Offset<BigDecimal> EPSILON = Assertions.within(new BigDecimal("0.00000000000000000001"));

    @Test
    @DisplayName("sin()")
    void sinTest() {
        assertThat(TrigonometryFunctions.sin(ZERO)).isCloseTo(ZERO, EPSILON);
        assertThat(TrigonometryFunctions.sin(new BigDecimal("1.57079632679489661923"))).isCloseTo(ONE, EPSILON);
    }

    @Test
    @DisplayName("cos()")
    void cosTest() {
        assertThat(TrigonometryFunctions.cos(ZERO)).isCloseTo(ONE, EPSILON);
        assertThat(TrigonometryFunctions.cos(new BigDecimal("1.57079632679489661923"))).isCloseTo(ZERO, EPSILON);
    }

    @Test
    @DisplayName("tan()")
    void tanTest() {
        assertThat(TrigonometryFunctions.tan(ZERO)).isCloseTo(ZERO, EPSILON);
    }

    @Test
    @DisplayName("asin()")
    void asinTest() {
        assertThat(TrigonometryFunctions.asin(ZERO)).isCloseTo(ZERO, EPSILON);
        assertThat(TrigonometryFunctions.asin(ONE)).isCloseTo(new BigDecimal("1.57079632679489661923"), EPSILON);
    }

    @Test
    @DisplayName("acos()")
    void acosTest() {
        assertThat(TrigonometryFunctions.acos(ONE)).isCloseTo(ZERO, EPSILON);
        assertThat(TrigonometryFunctions.acos(ZERO)).isCloseTo(new BigDecimal("1.57079632679489661923"), EPSILON);
    }

    @Test
    @DisplayName("atan()")
    void atanTest() {
        assertThat(TrigonometryFunctions.atan(ZERO)).isCloseTo(ZERO, EPSILON);
    }

    @Test
    @DisplayName("atan2()")
    void atan2Test() {
        assertThat(TrigonometryFunctions.atan2(ZERO, ONE)).isCloseTo(ZERO, EPSILON);
        assertThat(TrigonometryFunctions.atan2(ONE, ZERO)).isCloseTo(new BigDecimal("1.57079632679489661923"), EPSILON);
    }

    @Test
    @DisplayName("sinh()")
    void sinhTest() {
        assertThat(TrigonometryFunctions.sinh(ZERO)).isCloseTo(ZERO, EPSILON);
    }

    @Test
    @DisplayName("cosh()")
    void coshTest() {
        assertThat(TrigonometryFunctions.cosh(ZERO)).isCloseTo(ONE, EPSILON);
    }

    @Test
    @DisplayName("tanh()")
    void tanhTest() {
        assertThat(TrigonometryFunctions.tanh(ZERO)).isCloseTo(ZERO, EPSILON);
    }

    @Test
    @DisplayName("asinh()")
    void asinhTest() {
        assertThat(TrigonometryFunctions.asinh(ZERO)).isCloseTo(ZERO, EPSILON);
    }

    @Test
    @DisplayName("acosh()")
    void acoshTest() {
        assertThat(TrigonometryFunctions.acosh(ONE)).isCloseTo(ZERO, EPSILON);
    }

    @Test
    @DisplayName("atanh()")
    void atanhTest() {
        assertThat(TrigonometryFunctions.atanh(ZERO)).isCloseTo(ZERO, EPSILON);
    }
}
