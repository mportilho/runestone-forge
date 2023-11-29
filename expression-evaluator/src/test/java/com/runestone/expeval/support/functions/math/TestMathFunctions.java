package com.runestone.expeval.support.functions.math;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import static java.math.BigDecimal.valueOf;

public class TestMathFunctions {

    @Test
    public void testGeometricMeanFunction() {
        BigDecimal[] values = {valueOf(0.275e-9), valueOf(8.8e3)};
        Assertions.assertThat(MathFunctions.geometricMean(values, new MathContext(3, RoundingMode.HALF_EVEN))).isEqualByComparingTo("0.00156");

    }

    @Test
    public void testHarmonicMeanFunction() {
        BigDecimal[] values = {valueOf(1), valueOf(2), valueOf(4)};
        Assertions.assertThat(MathFunctions.harmonicMean(values, MathContext.DECIMAL64)).isEqualByComparingTo("1.714285714285714");
    }

    @Test
    public void testVarianceFunction() {
        BigDecimal[] values = {valueOf(206), valueOf(76), valueOf(-224), valueOf(36), valueOf(-94)};
        Assertions.assertThat(MathFunctions.variance(values, 0, MathContext.DECIMAL64)).isEqualByComparingTo("21704");
    }

    @Test
    public void testStdDev() {
        BigDecimal[] values = {valueOf(206), valueOf(76), valueOf(-224), valueOf(36), valueOf(-94)};
        Assertions.assertThat(MathFunctions.stdDev(values, 0, MathContext.DECIMAL64)).isEqualByComparingTo("147.3227748856232");
    }

    @Test
    public void testMeanDev() {
        BigDecimal[] values = {valueOf(3), valueOf(6), valueOf(6), valueOf(7), valueOf(8), valueOf(11), valueOf(15), valueOf(16)};
        Assertions.assertThat(MathFunctions.meanDev(values, MathContext.DECIMAL64)).isEqualByComparingTo("3.75");
    }

    @Test
    public void testRule3d() {
        BigDecimal v1 = valueOf(2);
        BigDecimal v2 = valueOf(800);
        BigDecimal v3 = valueOf(5);
        Assertions.assertThat(MathFunctions.rule3d(v1, v2, v3, MathContext.DECIMAL64)).isEqualByComparingTo("2000");

    }

    @Test
    public void testRule3i() {
        BigDecimal v1 = valueOf(6);
        BigDecimal v2 = valueOf(4);
        BigDecimal v3 = valueOf(22);
        Assertions.assertThat(MathFunctions.rule3i(v1, v2, v3, MathContext.DECIMAL64)).isEqualByComparingTo("33");
    }

}
