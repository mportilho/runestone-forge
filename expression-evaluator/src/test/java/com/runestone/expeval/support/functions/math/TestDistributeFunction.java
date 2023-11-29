package com.runestone.expeval.support.functions.math;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static java.math.BigDecimal.valueOf;

public class TestDistributeFunction {

    // ---------------------------------------- Positive Value, Normal Order  ----------------------------------------

    @Test
    public void testPositiveValueReachingAllUpperLimitsWithZeroRemainder() {
        BigDecimal[] result = MathFunctions.distribute(
                valueOf(18),
                BigDecimal.ONE,
                new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO},
                new BigDecimal[]{valueOf(3), valueOf(6), valueOf(9)});
        Assertions.assertThat(result).containsExactly(valueOf(3), valueOf(6), valueOf(9), valueOf(0));
    }

    @Test
    public void testPositiveValueReachingAllUpperLimitsWithSomeRemainder() {
        BigDecimal[] result = MathFunctions.distribute(
                valueOf(20),
                BigDecimal.ONE,
                new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO},
                new BigDecimal[]{valueOf(3), valueOf(6), valueOf(9)});
        Assertions.assertThat(result).containsExactly(valueOf(3), valueOf(6), valueOf(9), valueOf(2));
    }

    @Test
    public void testPositiveValueReachingNoUpperLimit() {
        BigDecimal[] result = MathFunctions.distribute(
                valueOf(20),
                BigDecimal.ONE,
                new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO},
                new BigDecimal[]{valueOf(30), valueOf(60), valueOf(90)});
        Assertions.assertThat(result).containsExactly(valueOf(20), valueOf(0), valueOf(0), valueOf(0));
    }

    @Test
    public void testPositiveValueReachingSomeUpperLimits() {
        BigDecimal[] result = MathFunctions.distribute(
                valueOf(8),
                BigDecimal.ONE,
                new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO},
                new BigDecimal[]{valueOf(3), valueOf(6), valueOf(9)});
        Assertions.assertThat(result).containsExactly(valueOf(3), valueOf(5), valueOf(0), valueOf(0));
    }

    @Test
    public void testPositiveValueReachingAllUpperLimitsWithZeroRemainder_NegativeTargets() {
        BigDecimal[] result = MathFunctions.distribute(
                valueOf(36),
                BigDecimal.ONE,
                new BigDecimal[]{valueOf(-3), valueOf(-6), valueOf(-9)},
                new BigDecimal[]{valueOf(3), valueOf(6), valueOf(9)});
        Assertions.assertThat(result).containsExactly(valueOf(3), valueOf(6), valueOf(9), valueOf(0));
    }

    @Test
    public void testPositiveValueReachingAllUpperLimitsWithSomeRemainder_NegativeTargets() {
        BigDecimal[] result = MathFunctions.distribute(
                valueOf(38),
                BigDecimal.ONE,
                new BigDecimal[]{valueOf(-3), valueOf(-6), valueOf(-9)},
                new BigDecimal[]{valueOf(3), valueOf(6), valueOf(9)});
        Assertions.assertThat(result).containsExactly(valueOf(3), valueOf(6), valueOf(9), valueOf(2));
    }

    @Test
    public void testPositiveValueReachingNoUpperLimit_NegativeTargets() {
        BigDecimal[] result = MathFunctions.distribute(
                valueOf(20),
                BigDecimal.ONE,
                new BigDecimal[]{valueOf(-30), valueOf(-60), valueOf(-90)},
                new BigDecimal[]{valueOf(30), valueOf(60), valueOf(90)});
        Assertions.assertThat(result).containsExactly(valueOf(-10), valueOf(-60), valueOf(-90), valueOf(0));
    }

    @Test
    public void testPositiveValueReachingSomeUpperLimits_NegativeTargets() {
        BigDecimal[] result = MathFunctions.distribute(
                valueOf(16),
                BigDecimal.ONE,
                new BigDecimal[]{valueOf(-3), valueOf(-6), valueOf(-9)},
                new BigDecimal[]{valueOf(3), valueOf(6), valueOf(9)});
        Assertions.assertThat(result).containsExactly(valueOf(3), valueOf(4), valueOf(-9), valueOf(0));
    }

    // ---------------------------------------- Positive Value, Inverse Order  ----------------------------------------

    @Test
    public void testPositiveValueInverseReachingAllUpperLimitsWithZeroRemainder() {
        BigDecimal[] result = MathFunctions.distribute(
                valueOf(18),
                valueOf(-1),
                new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO},
                new BigDecimal[]{valueOf(3), valueOf(6), valueOf(9)});
        Assertions.assertThat(result).containsExactly(valueOf(3), valueOf(6), valueOf(9), valueOf(0));
    }

    @Test
    public void testPositiveValueInverseReachingAllUpperLimitsWithSomeRemainder() {
        BigDecimal[] result = MathFunctions.distribute(
                valueOf(20),
                valueOf(-1),
                new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO},
                new BigDecimal[]{valueOf(3), valueOf(6), valueOf(9)});
        Assertions.assertThat(result).containsExactly(valueOf(3), valueOf(6), valueOf(9), valueOf(2));
    }

    @Test
    public void testPositiveValueInverseReachingNoUpperLimit() {
        BigDecimal[] result = MathFunctions.distribute(
                valueOf(20),
                valueOf(-1),
                new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO},
                new BigDecimal[]{valueOf(30), valueOf(60), valueOf(90)});
        Assertions.assertThat(result).containsExactly(valueOf(0), valueOf(0), valueOf(20), valueOf(0));
    }

    @Test
    public void testPositiveValueInverseReachingSomeUpperLimits() {
        BigDecimal[] result = MathFunctions.distribute(
                valueOf(12),
                valueOf(-1),
                new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO},
                new BigDecimal[]{valueOf(3), valueOf(6), valueOf(9)});
        Assertions.assertThat(result).containsExactly(valueOf(0), valueOf(3), valueOf(9), valueOf(0));
    }

    // ---------------------------------------- Negative Value, Normal Order  ----------------------------------------

    @Test
    public void testNegativeValueReachingAllLowerLimitsWithZeroRemainder() {
        BigDecimal[] result = MathFunctions.distribute(
                valueOf(-18),
                BigDecimal.ONE,
                new BigDecimal[]{valueOf(3), valueOf(6), valueOf(9)},
                new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO});
        Assertions.assertThat(result).containsExactly(valueOf(0), valueOf(0), valueOf(0), valueOf(0));
    }

    @Test
    public void testNegativeValueReachingAllLowerLimitsWithSomeRemainder() {
        BigDecimal[] result = MathFunctions.distribute(
                valueOf(-20),
                BigDecimal.ONE,
                new BigDecimal[]{valueOf(3), valueOf(6), valueOf(9)},
                new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO});
        Assertions.assertThat(result).containsExactly(valueOf(0), valueOf(0), valueOf(0), valueOf(-2));
    }

    @Test
    public void testNegativeValueReachingNoLowerLimit() {
        BigDecimal[] result = MathFunctions.distribute(
                valueOf(-20),
                BigDecimal.ONE,
                new BigDecimal[]{valueOf(30), valueOf(60), valueOf(90)},
                new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO});
        Assertions.assertThat(result).containsExactly(valueOf(10), valueOf(60), valueOf(90), valueOf(0));
    }

    @Test
    public void testNegativeValueReachingSomeLowerLimits() {
        BigDecimal[] result = MathFunctions.distribute(
                valueOf(-8),
                BigDecimal.ONE,
                new BigDecimal[]{valueOf(3), valueOf(6), valueOf(9)},
                new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO});
        Assertions.assertThat(result).containsExactly(valueOf(0), valueOf(1), valueOf(9), valueOf(0));
    }

    @Test
    public void testNegativeValueReachingAllLowerLimitsWithZeroRemainder_NegativeTargets() {
        BigDecimal[] result = MathFunctions.distribute(
                valueOf(-18),
                BigDecimal.ONE,
                new BigDecimal[]{valueOf(-3), valueOf(-6), valueOf(-9)},
                new BigDecimal[]{valueOf(-6), valueOf(-12), valueOf(-18)});
        Assertions.assertThat(result).containsExactly(valueOf(-6), valueOf(-12), valueOf(-18), valueOf(0));
    }

    @Test
    public void testNegativeValueReachingAllLowerLimitsWithSomeRemainder_NegativeTargets() {
        BigDecimal[] result = MathFunctions.distribute(
                valueOf(-20),
                BigDecimal.ONE,
                new BigDecimal[]{valueOf(-3), valueOf(-6), valueOf(-9)},
                new BigDecimal[]{valueOf(-6), valueOf(-12), valueOf(-18)});
        Assertions.assertThat(result).containsExactly(valueOf(-6), valueOf(-12), valueOf(-18), valueOf(-2));
    }

    @Test
    public void testNegativeValueReachingNoLowerLimit_NegativeTargets() {
        BigDecimal[] result = MathFunctions.distribute(
                valueOf(-20),
                BigDecimal.ONE,
                new BigDecimal[]{valueOf(-3), valueOf(-6), valueOf(-9)},
                new BigDecimal[]{valueOf(-30), valueOf(-60), valueOf(-90)});
        Assertions.assertThat(result).containsExactly(valueOf(-23), valueOf(-6), valueOf(-9), valueOf(0));
    }

    @Test
    public void testNegativeValueReachingSomeLowerLimits_NegativeTargets() {
        BigDecimal[] result = MathFunctions.distribute(
                valueOf(-16),
                BigDecimal.ONE,
                new BigDecimal[]{valueOf(-3), valueOf(-6), valueOf(-9)},
                new BigDecimal[]{valueOf(-6), valueOf(-12), valueOf(-18)});
        Assertions.assertThat(result).containsExactly(valueOf(-6), valueOf(-12), valueOf(-16), valueOf(0));
    }

    // ---------------------------------------- Negative Value, Inverse Order  ----------------------------------------

    @Test
    public void testNegativeValueInverseReachingAllLowerLimitsWithZeroRemainder() {
        BigDecimal[] result = MathFunctions.distribute(
                valueOf(-18),
                valueOf(-1),
                new BigDecimal[]{valueOf(3), valueOf(6), valueOf(9)},
                new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO});
        Assertions.assertThat(result).containsExactly(valueOf(0), valueOf(0), valueOf(0), valueOf(0));
    }

    @Test
    public void testNegativeValueInverseReachingAllLowerLimitsWithSomeRemainder() {
        BigDecimal[] result = MathFunctions.distribute(
                valueOf(-20),
                valueOf(-1),
                new BigDecimal[]{valueOf(3), valueOf(6), valueOf(9)},
                new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO});
        Assertions.assertThat(result).containsExactly(valueOf(0), valueOf(0), valueOf(0), valueOf(-2));
    }

    @Test
    public void testNegativeValueInverseReachingNoLowerLimit() {
        BigDecimal[] result = MathFunctions.distribute(
                valueOf(-20),
                valueOf(-1),
                new BigDecimal[]{valueOf(30), valueOf(60), valueOf(90)},
                new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO});
        Assertions.assertThat(result).containsExactly(valueOf(30), valueOf(60), valueOf(70), valueOf(0));
    }

    @Test
    public void testNegativeValueInverseReachingSomeLowerLimits() {
        BigDecimal[] result = MathFunctions.distribute(
                valueOf(-11),
                valueOf(-1),
                new BigDecimal[]{valueOf(3), valueOf(6), valueOf(9)},
                new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO});
        Assertions.assertThat(result).containsExactly(valueOf(3), valueOf(4), valueOf(0), valueOf(0));
    }

    // ---------------------------------------- Others  ----------------------------------------

    @Test
    public void testDistributeWithZeroValue() {
        BigDecimal[] result = MathFunctions.distribute(
                BigDecimal.ZERO,
                BigDecimal.ONE,
                new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO},
                new BigDecimal[]{valueOf(3), valueOf(6), valueOf(9)});
        Assertions.assertThat(result).containsExactly(valueOf(0), valueOf(0), valueOf(0), valueOf(0));
    }

    @Test
    public void testDifferentScales() {
        BigDecimal[] result = MathFunctions.distribute(
                new BigDecimal("20.004000"), // must have same scale of the remainder
                BigDecimal.ONE,
                new BigDecimal[]{new BigDecimal("0.01"), new BigDecimal("0.0002"), new BigDecimal("0.0003")},
                new BigDecimal[]{new BigDecimal("3.0001"), new BigDecimal("6.01"), new BigDecimal("9.00")});
        Assertions.assertThat(result).containsExactly(new BigDecimal("3.0001"), new BigDecimal("6.01"), new BigDecimal("9.00"), new BigDecimal("2.004400"));
    }

}
