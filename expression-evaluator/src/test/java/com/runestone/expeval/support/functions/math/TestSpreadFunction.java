package com.runestone.expeval.support.functions.math;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

public class TestSpreadFunction {

    @Test
    public void testSpreadValues() {
        BigDecimal[] result = MathFunctions.spread(
                new BigDecimal("1.00"),
                BigDecimal.ONE,
                new BigDecimal[]{new BigDecimal("-1"), new BigDecimal("2"), new BigDecimal("3")});
        Assertions.assertThat(result).containsExactly(new BigDecimal("-0.25"), new BigDecimal("0.50"), new BigDecimal("0.75"));
    }

    @Test
    public void testSpreadValueWithBiggerScale() {
        BigDecimal[] result = MathFunctions.spread(
                new BigDecimal("10.000"),
                BigDecimal.ONE,
                new BigDecimal[]{new BigDecimal("5"), new BigDecimal("15"), new BigDecimal("30")});
        Assertions.assertThat(result).containsExactly(new BigDecimal("1.000"), new BigDecimal("3.000"), new BigDecimal("6.000"));
    }

    @Test
    public void testSpreadWithDifferentScales() {
        BigDecimal[] result = MathFunctions.spread(
                new BigDecimal("951.41"),
                BigDecimal.ONE,
                new BigDecimal[]{new BigDecimal("10"), new BigDecimal("114.22"), new BigDecimal("1053.25")});
        Assertions.assertThat(result).containsExactly(new BigDecimal("8.08"), new BigDecimal("92.29"), new BigDecimal("851.04"));


//        BigDecimal[] valoresDistribuidos = CalculoUtils.distribuirSaldo(new BigDecimal("951.41"),
//                new BigDecimal[]{new BigDecimal("10"), new BigDecimal("114.22"), new BigDecimal("1053.25")});
//
//        Assertions.assertThat(valoresDistribuidos[0]).as("principal").isEqualTo(new BigDecimal("8.08"));
//        Assertions.assertThat(valoresDistribuidos[1]).as("correcao").isEqualTo(new BigDecimal("92.29"));
//        Assertions.assertThat(valoresDistribuidos[2]).as("juros").isEqualTo(new BigDecimal("851.04"));
//        Assertions.assertThat(valoresDistribuidos[3]).as("resto").isEqualTo(new BigDecimal("0.00"));
    }

    @Test
    public void testSpreadZero() {
        BigDecimal[] result = MathFunctions.spread(
                new BigDecimal("0.0"),
                BigDecimal.ONE,
                new BigDecimal[]{new BigDecimal("10"), new BigDecimal("114.22"), new BigDecimal("1053.25")});
        Assertions.assertThat(result).containsExactly(new BigDecimal("0.0"), new BigDecimal("0.0"), new BigDecimal("0.0"));
    }

    @Test
    public void testSpreadValueWithZeroSumReferences() {
        BigDecimal[] result1 = MathFunctions.spread(
                new BigDecimal("1000.00"),
                BigDecimal.ONE,
                new BigDecimal[]{new BigDecimal("0"), new BigDecimal("0"), new BigDecimal("0")});
        Assertions.assertThat(result1).containsExactly(new BigDecimal("333.34"), new BigDecimal("333.33"), new BigDecimal("333.33"));

        BigDecimal[] result2 = MathFunctions.spread(
                new BigDecimal("1000.00"),
                BigDecimal.ONE,
                new BigDecimal[]{new BigDecimal("2"), new BigDecimal("-1"), new BigDecimal("-1")});
        Assertions.assertThat(result2).containsExactly(new BigDecimal("333.34"), new BigDecimal("333.33"), new BigDecimal("333.33"));
    }

    @Test
    public void testSpreadValueWithZeroSumReferences_inverse() {
        BigDecimal[] result1 = MathFunctions.spread(
                new BigDecimal("1000.00"),
                BigDecimal.valueOf(-1),
                new BigDecimal[]{new BigDecimal("0"), new BigDecimal("0"), new BigDecimal("0")});
        Assertions.assertThat(result1).containsExactly(new BigDecimal("333.33"), new BigDecimal("333.33"), new BigDecimal("333.34"));

        BigDecimal[] result2 = MathFunctions.spread(
                new BigDecimal("1000.00"),
                BigDecimal.valueOf(-1),
                new BigDecimal[]{new BigDecimal("2"), new BigDecimal("-1"), new BigDecimal("-1")});
        Assertions.assertThat(result2).containsExactly(new BigDecimal("333.33"), new BigDecimal("333.33"), new BigDecimal("333.34"));
    }

    @Test
    public void testNegativeRemainder() {
        BigDecimal[] result1 = MathFunctions.spread(
                new BigDecimal("1500"),
                BigDecimal.ONE,
                new BigDecimal[]{new BigDecimal("100"), new BigDecimal("1142.2"), new BigDecimal("10532.5")});
        Assertions.assertThat(result1).containsExactly(new BigDecimal("12"), new BigDecimal("146"), new BigDecimal("1342"));

        BigDecimal[] result2 = MathFunctions.spread(
                new BigDecimal("1500"),
                BigDecimal.valueOf(-1),
                new BigDecimal[]{new BigDecimal("100"), new BigDecimal("1142.2"), new BigDecimal("10532.5")});
        Assertions.assertThat(result2).containsExactly(new BigDecimal("13"), new BigDecimal("146"), new BigDecimal("1341"));
    }

}
