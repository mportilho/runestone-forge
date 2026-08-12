package com.runestone.expeval_mk3.internal.runtime;

import ch.obermuhlner.math.big.BigDecimalMath;
import com.runestone.expeval_mk3.api.ExpressionExecutionException;
import com.runestone.expeval_mk3.api.SourceSpan;
import com.runestone.expeval_mk3.internal.diagnostics.DiagnosticCode;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * ADR 0017 real-domain adapters. Every test observes {@link BigMathAdapter} directly through
 * {@link RecordingAdapter} to prove the adapted paths never submit a negative base to {@code pow} nor a
 * negative degree to {@code root}, and that magnitude always travels through the big-math seam, even for
 * apparently exact integral powers and roots.
 */
class RealDomainArithmeticTest {

    private static final SourceSpan SPAN = new SourceSpan(0, 1, 1, 1);
    private static final MathContext CEILING_CONTEXT = new MathContext(12, RoundingMode.CEILING);
    private static final MathContext FLOOR_CONTEXT = new MathContext(12, RoundingMode.FLOOR);
    private static final MathContext HALF_UP_CONTEXT = new MathContext(12, RoundingMode.HALF_UP);

    // --- pow: zero conventions ---

    @Test
    void zeroToZeroIsOne() {
        RecordingAdapter adapter = new RecordingAdapter();
        BigDecimal result = RealDomainArithmetic.pow(BigDecimal.ZERO, BigDecimal.ZERO, HALF_UP_CONTEXT, SPAN, adapter);
        assertThat(result).isEqualByComparingTo(BigDecimal.ONE);
        assertThat(adapter.powCalls).hasSize(1);
    }

    @Test
    void zeroToPositiveExponentIsZero() {
        RecordingAdapter adapter = new RecordingAdapter();
        BigDecimal result = RealDomainArithmetic.pow(
                BigDecimal.ZERO, new BigDecimal("3"), HALF_UP_CONTEXT, SPAN, adapter);
        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(adapter.powCalls).hasSize(1);
    }

    @Test
    void zeroToNegativeExponentIsUndefinedWithoutCallingBigMath() {
        RecordingAdapter adapter = new RecordingAdapter();
        assertThatThrownBy(() -> RealDomainArithmetic.pow(
                        BigDecimal.ZERO, new BigDecimal("-2"), HALF_UP_CONTEXT, SPAN, adapter))
                .isInstanceOfSatisfying(ExpressionExecutionException.class, exception ->
                        assertThat(exception.diagnostic().code()).isEqualTo(DiagnosticCode.RUNTIME_POWER_UNDEFINED.name()));
        assertThat(adapter.powCalls).isEmpty();
    }

    // --- pow: negative base adapter ---

    @Test
    void negativeBaseOddReducedNumeratorNegatesMagnitudeWithSwappedContext() {
        RecordingAdapter adapter = new RecordingAdapter();
        BigDecimal result = RealDomainArithmetic.pow(
                new BigDecimal("-32"), new BigDecimal("0.2"), CEILING_CONTEXT, SPAN, adapter);
        assertThatIsCloseTo(result, new BigDecimal("-2"));
        assertThat(adapter.powCalls).hasSize(1);
        BigDecimal[] call = adapter.powCalls.getFirst();
        assertThat(call[0]).isEqualByComparingTo(new BigDecimal("32"));
        assertThat(adapter.powContexts.getFirst().getRoundingMode()).isEqualTo(RoundingMode.FLOOR);
        assertThat(adapter.powContexts.getFirst().getPrecision()).isEqualTo(CEILING_CONTEXT.getPrecision());
    }

    @Test
    void negativeBaseEvenReducedNumeratorReturnsPositiveMagnitudeWithOriginalContext() {
        RecordingAdapter adapter = new RecordingAdapter();
        BigDecimal result = RealDomainArithmetic.pow(
                new BigDecimal("-32"), new BigDecimal("0.4"), CEILING_CONTEXT, SPAN, adapter);
        assertThatIsCloseTo(result, new BigDecimal("4"));
        assertThat(adapter.powContexts.getFirst()).isEqualTo(CEILING_CONTEXT);
    }

    private static void assertThatIsCloseTo(BigDecimal actual, BigDecimal expected) {
        // The general exp(y*log(x)) big-math path for a non-integral exponent is a numeric
        // approximation, not an exact rational result; only classification (sign, reality) is exact.
        assertThat(actual.subtract(expected).abs()).isLessThan(new BigDecimal("0.000000001"));
    }

    @Test
    void negativeBaseEvenReducedDenominatorIsComplexDomainWithoutCallingBigMath() {
        RecordingAdapter adapter = new RecordingAdapter();
        assertThatThrownBy(() -> RealDomainArithmetic.pow(
                        new BigDecimal("-8"), new BigDecimal("0.5"), HALF_UP_CONTEXT, SPAN, adapter))
                .isInstanceOfSatisfying(ExpressionExecutionException.class, exception -> assertThat(
                        exception.diagnostic().code()).isEqualTo(DiagnosticCode.RUNTIME_POWER_COMPLEX_DOMAIN.name()));
        assertThat(adapter.powCalls).isEmpty();
    }

    @Test
    void negativeBaseIntegerExponentStillUsesBigMathSeam() {
        RecordingAdapter adapter = new RecordingAdapter();
        BigDecimal result = RealDomainArithmetic.pow(
                new BigDecimal("-2"), new BigDecimal("3"), HALF_UP_CONTEXT, SPAN, adapter);
        assertThat(result).isEqualByComparingTo(new BigDecimal("-8"));
        assertThat(adapter.powCalls).hasSize(1);
    }

    @Test
    void nonCeilingFloorRoundingModeIsUnchangedForNegativeResult() {
        RecordingAdapter adapter = new RecordingAdapter();
        RealDomainArithmetic.pow(new BigDecimal("-32"), new BigDecimal("0.2"), HALF_UP_CONTEXT, SPAN, adapter);
        assertThat(adapter.powContexts.getFirst()).isEqualTo(HALF_UP_CONTEXT);
    }

    @Test
    void negativeResultPreservesTheBigMathProducedScaleWithoutNormalization() {
        RecordingAdapter adapter = new RecordingAdapter();
        BigDecimal result = RealDomainArithmetic.pow(
                new BigDecimal("-3"), new BigDecimal("3"), HALF_UP_CONTEXT, SPAN, adapter);
        BigDecimal positiveMagnitude = BigDecimalMath.pow(new BigDecimal("3"), new BigDecimal("3"), HALF_UP_CONTEXT);
        assertThat(result.scale()).isEqualTo(positiveMagnitude.scale());
        assertThat(result).isEqualByComparingTo(new BigDecimal("-27"));
    }

    @Test
    void positiveBaseFractionalExponentUsesBigMathDirectly() {
        RecordingAdapter adapter = new RecordingAdapter();
        BigDecimal result = RealDomainArithmetic.pow(new BigDecimal("2"), new BigDecimal("0.5"), HALF_UP_CONTEXT, SPAN, adapter);
        assertThat(result).isEqualByComparingTo(BigDecimalMath.pow(new BigDecimal("2"), new BigDecimal("0.5"), HALF_UP_CONTEXT));
        assertThat(adapter.powCalls).hasSize(1);
    }

    @Test
    void positiveBaseNegativeExponentProducesReciprocalThroughBigMath() {
        RecordingAdapter adapter = new RecordingAdapter();
        BigDecimal result = RealDomainArithmetic.pow(new BigDecimal("2"), new BigDecimal("-3"), HALF_UP_CONTEXT, SPAN, adapter);
        assertThat(result).isEqualByComparingTo(new BigDecimal("0.125"));
        assertThat(adapter.powCalls).hasSize(1);
        assertThat(adapter.powCalls.getFirst()[1]).isEqualByComparingTo(new BigDecimal("-3"));
    }

    // --- pow: domainProven fast path (issue #124) ---

    @Test
    void domainProvenSkipsClassificationAndCallsAdapterDirectly() {
        RecordingAdapter adapter = new RecordingAdapter();
        BigDecimal result = RealDomainArithmetic.powWithProvenDomain(
                new BigDecimal("2"), new BigDecimal("0.5"), HALF_UP_CONTEXT, SPAN, adapter);
        assertThat(result).isEqualByComparingTo(BigDecimalMath.pow(new BigDecimal("2"), new BigDecimal("0.5"), HALF_UP_CONTEXT));
        assertThat(adapter.powCalls).hasSize(1);
        assertThat(adapter.powContexts.getFirst()).isEqualTo(HALF_UP_CONTEXT);
    }

    @Property
    void domainProvenMatchesClassifyingPathForEveryPositiveBaseAndExponent(
            @ForAll("nonZeroDecimals") BigDecimal exponent) {
        BigDecimal base = new BigDecimal("7");
        BigDecimal classified = RealDomainArithmetic.pow(base, exponent, HALF_UP_CONTEXT, SPAN, BigMathAdapter.DEFAULT);
        BigDecimal proven = RealDomainArithmetic.powWithProvenDomain(base, exponent, HALF_UP_CONTEXT, SPAN, BigMathAdapter.DEFAULT);
        assertThat(proven).isEqualByComparingTo(classified);
    }

    // --- root: zero and degree conventions ---

    @Test
    void degreeZeroIsUndefinedWithoutCallingBigMath() {
        RecordingAdapter adapter = new RecordingAdapter();
        assertThatThrownBy(() -> RealDomainArithmetic.root(
                        BigDecimal.ZERO, new BigDecimal("8"), HALF_UP_CONTEXT, SPAN, adapter))
                .isInstanceOfSatisfying(ExpressionExecutionException.class, exception ->
                        assertThat(exception.diagnostic().code()).isEqualTo(DiagnosticCode.RUNTIME_ROOT_UNDEFINED.name()));
        assertThat(adapter.rootCalls).isEmpty();
    }

    @Test
    void zeroRadicandWithPositiveDegreeIsZeroThroughBigMath() {
        RecordingAdapter adapter = new RecordingAdapter();
        BigDecimal result = RealDomainArithmetic.root(
                new BigDecimal("3"), BigDecimal.ZERO, HALF_UP_CONTEXT, SPAN, adapter);
        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(adapter.rootCalls).hasSize(1);
    }

    @Test
    void zeroRadicandWithNegativeDegreeIsUndefinedWithoutCallingBigMath() {
        RecordingAdapter adapter = new RecordingAdapter();
        assertThatThrownBy(() -> RealDomainArithmetic.root(
                        new BigDecimal("-3"), BigDecimal.ZERO, HALF_UP_CONTEXT, SPAN, adapter))
                .isInstanceOfSatisfying(ExpressionExecutionException.class, exception ->
                        assertThat(exception.diagnostic().code()).isEqualTo(DiagnosticCode.RUNTIME_ROOT_UNDEFINED.name()));
        assertThat(adapter.rootCalls).isEmpty();
        assertThat(adapter.reciprocalCalls).isEmpty();
    }

    // --- root: negative radicand adapter ---

    @Test
    void negativeRadicandEvenReducedDegreeNumeratorIsComplexDomain() {
        RecordingAdapter adapter = new RecordingAdapter();
        assertThatThrownBy(() -> RealDomainArithmetic.root(
                        new BigDecimal("2"), new BigDecimal("-8"), HALF_UP_CONTEXT, SPAN, adapter))
                .isInstanceOfSatisfying(ExpressionExecutionException.class, exception -> assertThat(
                        exception.diagnostic().code()).isEqualTo(DiagnosticCode.RUNTIME_ROOT_COMPLEX_DOMAIN.name()));
        assertThat(adapter.rootCalls).isEmpty();
    }

    @Test
    void negativeRadicandOddReducedDegreeNumeratorIsRealAndNegative() {
        RecordingAdapter adapter = new RecordingAdapter();
        BigDecimal result = RealDomainArithmetic.root(
                new BigDecimal("3"), new BigDecimal("-8"), CEILING_CONTEXT, SPAN, adapter);
        assertThat(result).isEqualByComparingTo(new BigDecimal("-2"));
        assertThat(adapter.rootCalls).hasSize(1);
        BigDecimal[] call = adapter.rootCalls.getFirst();
        assertThat(call[0]).isEqualByComparingTo(new BigDecimal("8"));
        assertThat(call[1]).isEqualByComparingTo(new BigDecimal("3"));
        assertThat(adapter.rootContexts.getFirst().getRoundingMode()).isEqualTo(RoundingMode.FLOOR);
        assertThat(adapter.reciprocalCalls).isEmpty();
    }

    // --- root: negative degree adapter ---

    @Test
    void negativeDegreeUsesReciprocalCompositionNeverPassedDirectlyToRoot() {
        RecordingAdapter adapter = new RecordingAdapter();
        BigDecimal result = RealDomainArithmetic.root(
                new BigDecimal("-3"), new BigDecimal("8"), HALF_UP_CONTEXT, SPAN, adapter);
        assertThat(result).isEqualByComparingTo(new BigDecimal("0.5"));
        assertThat(adapter.rootCalls).hasSize(1);
        BigDecimal[] rootCall = adapter.rootCalls.getFirst();
        assertThat(rootCall[0]).isEqualByComparingTo(new BigDecimal("8"));
        assertThat(rootCall[1].signum()).isEqualTo(1);
        assertThat(adapter.reciprocalCalls).hasSize(1);
    }

    @Test
    void negativeDegreeAndNegativeRadicandCombineBothAdapters() {
        RecordingAdapter adapter = new RecordingAdapter();
        BigDecimal result = RealDomainArithmetic.root(
                new BigDecimal("-3"), new BigDecimal("-8"), HALF_UP_CONTEXT, SPAN, adapter);
        assertThat(result).isEqualByComparingTo(new BigDecimal("-0.5"));
        assertThat(adapter.rootCalls).hasSize(1);
        assertThat(adapter.rootCalls.getFirst()[1].signum()).isEqualTo(1);
        assertThat(adapter.reciprocalCalls).hasSize(1);
    }

    @Test
    void negativeDegreeEvenNumeratorWithNegativeRadicandIsComplexDomain() {
        RecordingAdapter adapter = new RecordingAdapter();
        assertThatThrownBy(() -> RealDomainArithmetic.root(
                        new BigDecimal("-2"), new BigDecimal("-8"), HALF_UP_CONTEXT, SPAN, adapter))
                .isInstanceOfSatisfying(ExpressionExecutionException.class, exception -> assertThat(
                        exception.diagnostic().code()).isEqualTo(DiagnosticCode.RUNTIME_ROOT_COMPLEX_DOMAIN.name()));
        assertThat(adapter.rootCalls).isEmpty();
        assertThat(adapter.reciprocalCalls).isEmpty();
    }

    // --- calculation failure preserves cause ---

    @Test
    void bigMathFailureBecomesCalculationFailureWithCausePreserved() {
        RuntimeException cause = new ArithmeticException("boom");
        BigMathAdapter failingAdapter = new BigMathAdapter() {
            @Override
            public BigDecimal pow(BigDecimal base, BigDecimal exponent, MathContext mathContext) {
                throw cause;
            }

            @Override
            public BigDecimal root(BigDecimal radicand, BigDecimal degree, MathContext mathContext) {
                throw cause;
            }

            @Override
            public BigDecimal reciprocal(BigDecimal value, MathContext mathContext) {
                throw cause;
            }
        };
        assertThatThrownBy(() -> RealDomainArithmetic.pow(
                        new BigDecimal("2"), new BigDecimal("0.5"), HALF_UP_CONTEXT, SPAN, failingAdapter))
                .isInstanceOfSatisfying(ExpressionExecutionException.class, exception -> {
                    assertThat(exception.diagnostic().code()).isEqualTo(DiagnosticCode.RUNTIME_CALCULATION_FAILURE.name());
                    assertThat(exception.getCause()).isSameAs(cause);
                });
    }

    // --- independent-oracle property tests ---

    @Property
    void powClassificationAndSignMatchIndependentGcdOracle(@ForAll("nonZeroDecimals") BigDecimal exponent) {
        boolean[] parity = reducedParity(exponent);
        boolean numeratorOdd = parity[0];
        boolean denominatorOdd = parity[1];
        BigDecimal base = new BigDecimal("-7");
        RecordingAdapter adapter = new RecordingAdapter();
        if (!denominatorOdd) {
            assertThatThrownBy(() -> RealDomainArithmetic.pow(base, exponent, HALF_UP_CONTEXT, SPAN, adapter))
                    .isInstanceOfSatisfying(ExpressionExecutionException.class, exception -> assertThat(
                            exception.diagnostic().code()).isEqualTo(DiagnosticCode.RUNTIME_POWER_COMPLEX_DOMAIN.name()));
            assertThat(adapter.powCalls).isEmpty();
            return;
        }
        BigDecimal result = RealDomainArithmetic.pow(base, exponent, HALF_UP_CONTEXT, SPAN, adapter);
        assertThat(adapter.powCalls).hasSize(1);
        assertThat(adapter.powCalls.getFirst()[0].signum()).isEqualTo(1);
        assertThat(result.signum()).isEqualTo(numeratorOdd ? -1 : 1);
    }

    @Property
    void rootClassificationAndSignMatchIndependentGcdOracle(@ForAll("nonZeroDecimals") BigDecimal degree) {
        boolean[] parity = reducedParity(degree);
        boolean numeratorOdd = parity[0];
        boolean denominatorOdd = parity[1];
        BigDecimal radicand = new BigDecimal("-7");
        RecordingAdapter adapter = new RecordingAdapter();
        if (!numeratorOdd) {
            assertThatThrownBy(() -> RealDomainArithmetic.root(degree, radicand, HALF_UP_CONTEXT, SPAN, adapter))
                    .isInstanceOfSatisfying(ExpressionExecutionException.class, exception -> assertThat(
                            exception.diagnostic().code()).isEqualTo(DiagnosticCode.RUNTIME_ROOT_COMPLEX_DOMAIN.name()));
            assertThat(adapter.rootCalls).isEmpty();
            return;
        }
        BigDecimal result = RealDomainArithmetic.root(degree, radicand, HALF_UP_CONTEXT, SPAN, adapter);
        assertThat(adapter.rootCalls).hasSize(1);
        assertThat(adapter.rootCalls.getFirst()[0].signum()).isEqualTo(1);
        assertThat(adapter.rootCalls.getFirst()[1].signum()).isEqualTo(1);
        assertThat(result.signum()).isEqualTo(denominatorOdd ? -1 : 1);
    }

    @Test
    void extremeScaleDoesNotMaterializePowerOfTenForPow() {
        RecordingAdapter adapter = new RecordingAdapter();
        BigDecimal exponent = new BigDecimal(BigInteger.valueOf(3), 1_000_000_000);
        assertThatThrownBy(() -> RealDomainArithmetic.pow(
                        new BigDecimal("-4"), exponent, HALF_UP_CONTEXT, SPAN, adapter))
                .isInstanceOfSatisfying(ExpressionExecutionException.class, exception -> assertThat(
                        exception.diagnostic().code()).isEqualTo(DiagnosticCode.RUNTIME_POWER_COMPLEX_DOMAIN.name()));
    }

    @Test
    void extremeScaleDoesNotMaterializePowerOfTenForRootDegree() {
        // A magnitude-free adapter isolates classification from an astronomically large 1/degree
        // magnitude computation, which is irrelevant to what this test proves.
        List<BigDecimal[]> rootCalls = new ArrayList<>();
        BigMathAdapter magnitudeFreeAdapter = new BigMathAdapter() {
            @Override
            public BigDecimal pow(BigDecimal base, BigDecimal exponent, MathContext mathContext) {
                throw new UnsupportedOperationException();
            }

            @Override
            public BigDecimal root(BigDecimal radicand, BigDecimal degree, MathContext mathContext) {
                rootCalls.add(new BigDecimal[] {radicand, degree});
                return BigDecimal.ONE;
            }

            @Override
            public BigDecimal reciprocal(BigDecimal value, MathContext mathContext) {
                throw new UnsupportedOperationException();
            }
        };
        BigDecimal degree = new BigDecimal(BigInteger.valueOf(3), 1_000_000_000);
        BigDecimal result = RealDomainArithmetic.root(degree, new BigDecimal("-4"), HALF_UP_CONTEXT, SPAN, magnitudeFreeAdapter);
        assertThat(rootCalls).hasSize(1);
        assertThat(rootCalls.getFirst()[0].signum()).isEqualTo(1);
        assertThat(result.signum()).isEqualTo(1);
    }

    @Provide("nonZeroDecimals")
    Arbitrary<BigDecimal> nonZeroDecimals() {
        Arbitrary<Long> unscaled = Arbitraries.longs().between(-50L, 50L).filter(value -> value != 0L);
        Arbitrary<Integer> scale = Arbitraries.integers().between(0, 4);
        return Combinators.combine(unscaled, scale)
                .as((unscaledValue, scaleValue) -> new BigDecimal(BigInteger.valueOf(unscaledValue), scaleValue));
    }

    private static boolean[] reducedParity(BigDecimal value) {
        BigInteger unscaled = value.unscaledValue();
        int scale = value.scale();
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
        return new boolean[] {reducedNumerator.testBit(0), reducedDenominator.testBit(0)};
    }

    private static final class RecordingAdapter implements BigMathAdapter {

        private final List<BigDecimal[]> powCalls = new ArrayList<>();
        private final List<MathContext> powContexts = new ArrayList<>();
        private final List<BigDecimal[]> rootCalls = new ArrayList<>();
        private final List<MathContext> rootContexts = new ArrayList<>();
        private final List<BigDecimal[]> reciprocalCalls = new ArrayList<>();

        @Override
        public BigDecimal pow(BigDecimal base, BigDecimal exponent, MathContext mathContext) {
            powCalls.add(new BigDecimal[] {base, exponent});
            powContexts.add(mathContext);
            return BigDecimalMath.pow(base, exponent, mathContext);
        }

        @Override
        public BigDecimal root(BigDecimal radicand, BigDecimal degree, MathContext mathContext) {
            rootCalls.add(new BigDecimal[] {radicand, degree});
            rootContexts.add(mathContext);
            return BigDecimalMath.root(radicand, degree, mathContext);
        }

        @Override
        public BigDecimal reciprocal(BigDecimal value, MathContext mathContext) {
            reciprocalCalls.add(new BigDecimal[] {value});
            return BigDecimalMath.reciprocal(value, mathContext);
        }
    }
}
