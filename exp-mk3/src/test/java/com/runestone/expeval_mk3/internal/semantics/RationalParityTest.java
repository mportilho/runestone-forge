package com.runestone.expeval_mk3.internal.semantics;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;

class RationalParityTest {

    @Property
    void matchesIndependentGcdReductionOracle(
            @ForAll("unscaledValues") BigInteger unscaled, @ForAll("smallScales") int scale) {
        BigDecimal value = new BigDecimal(unscaled, scale);
        RationalParity actual = RationalParity.classify(value);
        RationalParity expected = reduceByGcdOracle(unscaled, scale);
        assertThat(actual).isEqualTo(expected);
    }

    @Provide("unscaledValues")
    net.jqwik.api.Arbitrary<BigInteger> unscaledValues() {
        return Arbitraries.longs().between(-1_000_000L, 1_000_000L).map(BigInteger::valueOf);
    }

    @Provide("smallScales")
    net.jqwik.api.Arbitrary<Integer> smallScales() {
        return Arbitraries.integers().between(-30, 30);
    }

    private static RationalParity reduceByGcdOracle(BigInteger unscaled, int scale) {
        if (unscaled.signum() == 0) {
            return new RationalParity(0, false, true);
        }
        BigInteger numerator;
        BigInteger denominator;
        if (scale >= 0) {
            numerator = unscaled;
            denominator = BigInteger.TEN.pow(scale);
        } else {
            numerator = unscaled.multiply(BigInteger.TEN.pow(-scale));
            denominator = BigInteger.ONE;
        }
        BigInteger gcd = numerator.gcd(denominator);
        BigInteger reducedNumerator = numerator.divide(gcd).abs();
        BigInteger reducedDenominator = denominator.divide(gcd);
        return new RationalParity(unscaled.signum(), reducedNumerator.testBit(0), reducedDenominator.testBit(0));
    }

    @Test
    void zeroIsIntegralAndEven() {
        RationalParity parity = RationalParity.classify(BigDecimal.ZERO);
        assertThat(parity.signum()).isZero();
        assertThat(parity.numeratorOdd()).isFalse();
        assertThat(parity.denominatorOdd()).isTrue();
    }

    @Test
    void onePointFiveReducesToThreeOverTwo() {
        RationalParity parity = RationalParity.classify(new BigDecimal("1.50"));
        assertThat(parity.signum()).isEqualTo(1);
        assertThat(parity.numeratorOdd()).isTrue();
        assertThat(parity.denominatorOdd()).isFalse();
    }

    @Test
    void twoPointZeroZeroReducesToTwoOverOne() {
        RationalParity parity = RationalParity.classify(new BigDecimal("2.00"));
        assertThat(parity.signum()).isEqualTo(1);
        assertThat(parity.numeratorOdd()).isFalse();
        assertThat(parity.denominatorOdd()).isTrue();
    }

    @Test
    void zeroPointTwoReducesToOneOverFive() {
        RationalParity parity = RationalParity.classify(new BigDecimal("0.2"));
        assertThat(parity.signum()).isEqualTo(1);
        assertThat(parity.numeratorOdd()).isTrue();
        assertThat(parity.denominatorOdd()).isTrue();
    }

    @Test
    void negativeValuePreservesMagnitudeParity() {
        RationalParity parity = RationalParity.classify(new BigDecimal("-1.50"));
        assertThat(parity.signum()).isEqualTo(-1);
        assertThat(parity.numeratorOdd()).isTrue();
        assertThat(parity.denominatorOdd()).isFalse();
    }

    @Test
    void extremePositiveScaleDoesNotMaterializePowerOfTen() {
        BigDecimal value = new BigDecimal(BigInteger.valueOf(3), 1_000_000_000);
        RationalParity parity = RationalParity.classify(value);
        assertThat(parity.signum()).isEqualTo(1);
        assertThat(parity.numeratorOdd()).isTrue();
        assertThat(parity.denominatorOdd()).isFalse();
    }

    @Test
    void extremeNegativeScaleDoesNotMaterializePowerOfTen() {
        BigDecimal value = new BigDecimal(BigInteger.valueOf(7), -1_000_000_000);
        RationalParity parity = RationalParity.classify(value);
        assertThat(parity.signum()).isEqualTo(1);
        assertThat(parity.numeratorOdd()).isFalse();
        assertThat(parity.denominatorOdd()).isTrue();
    }

    @Test
    void negateFlipsSignAndPreservesParity() {
        RationalParity parity = RationalParity.classify(new BigDecimal("1.50"));
        RationalParity negated = parity.negate();
        assertThat(negated.signum()).isEqualTo(-1);
        assertThat(negated.numeratorOdd()).isEqualTo(parity.numeratorOdd());
        assertThat(negated.denominatorOdd()).isEqualTo(parity.denominatorOdd());
    }
}
