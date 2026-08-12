package com.runestone.expeval_mk3.internal.runtime;

import com.runestone.expeval_mk3.api.SourceSpan;
import com.runestone.expeval_mk3.internal.diagnostics.DiagnosticCode;
import com.runestone.expeval_mk3.internal.diagnostics.RuntimeFailures;
import com.runestone.expeval_mk3.internal.semantics.RationalParity;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * ADR 0017 real-domain adapters for {@code ^} and {@code root}. Every finite decimal operand is
 * classified by its exact canonical reduced rational sign and parity via {@link RationalParity}, never
 * by materializing a denominator proportional to {@code 10^scale}; that classification decides whether
 * the operation is real, complex-domain, or undefined, and what sign the real result carries. Magnitude
 * is always calculated through {@link BigMathAdapter}, never {@code BigDecimal.pow}, using a context with
 * {@code CEILING}/{@code FLOOR} exchanged when the classified result will be negated, so sign restoration
 * is an exact negation of an already-correctly-rounded magnitude.
 */
final class RealDomainArithmetic {

    private RealDomainArithmetic() {
    }

    static BigDecimal pow(BigDecimal base, BigDecimal exponent, MathContext mathContext, SourceSpan sourceSpan) {
        return pow(base, exponent, mathContext, sourceSpan, BigMathAdapter.DEFAULT);
    }

    /**
     * Skips sign inspection and rational-parity classification entirely (issue #124): the resolver
     * already proved this exact node's base non-negative at every execution (a positive-literal base
     * with an arbitrary exponent, {@code PowerRealDomainDeferredCheck} not emitted), so re-deriving that
     * proof on every call would only repeat work already done once, at compile time.
     */
    static BigDecimal powWithProvenDomain(
            BigDecimal base, BigDecimal exponent, MathContext mathContext, SourceSpan sourceSpan) {
        return powWithProvenDomain(base, exponent, mathContext, sourceSpan, BigMathAdapter.DEFAULT);
    }

    static BigDecimal powWithProvenDomain(
            BigDecimal base, BigDecimal exponent, MathContext mathContext, SourceSpan sourceSpan, BigMathAdapter adapter) {
        return callPow(adapter, base, exponent, mathContext, sourceSpan);
    }

    static BigDecimal pow(
            BigDecimal base,
            BigDecimal exponent,
            MathContext mathContext,
            SourceSpan sourceSpan,
            BigMathAdapter adapter) {
        if (base.signum() == 0 && exponent.signum() < 0) {
            throw RuntimeFailures.domainViolation(
                    DiagnosticCode.RUNTIME_POWER_UNDEFINED,
                    "zero raised to a negative exponent is undefined",
                    sourceSpan);
        }
        if (base.signum() >= 0) {
            return callPow(adapter, base, exponent, mathContext, sourceSpan);
        }
        RationalParity exponentParity = RationalParity.classify(exponent);
        if (!exponentParity.denominatorOdd()) {
            throw RuntimeFailures.domainViolation(
                    DiagnosticCode.RUNTIME_POWER_COMPLEX_DOMAIN,
                    "negative base with an exponent whose reduced denominator is even requires a complex result",
                    sourceSpan);
        }
        boolean negativeResult = exponentParity.numeratorOdd();
        MathContext magnitudeContext = negativeResult ? swapDirectionalRounding(mathContext) : mathContext;
        BigDecimal magnitude = callPow(adapter, base.abs(), exponent, magnitudeContext, sourceSpan);
        return negativeResult ? magnitude.negate() : magnitude;
    }

    static BigDecimal root(BigDecimal degree, BigDecimal radicand, MathContext mathContext, SourceSpan sourceSpan) {
        return root(degree, radicand, mathContext, sourceSpan, BigMathAdapter.DEFAULT);
    }

    static BigDecimal root(
            BigDecimal degree,
            BigDecimal radicand,
            MathContext mathContext,
            SourceSpan sourceSpan,
            BigMathAdapter adapter) {
        if (degree.signum() == 0) {
            throw RuntimeFailures.domainViolation(
                    DiagnosticCode.RUNTIME_ROOT_UNDEFINED, "root degree zero is undefined", sourceSpan);
        }
        if (radicand.signum() == 0 && degree.signum() < 0) {
            throw RuntimeFailures.domainViolation(
                    DiagnosticCode.RUNTIME_ROOT_UNDEFINED,
                    "zero radicand with a negative degree is undefined",
                    sourceSpan);
        }
        boolean negativeResult = false;
        if (radicand.signum() < 0) {
            RationalParity degreeParity = RationalParity.classify(degree);
            if (!degreeParity.numeratorOdd()) {
                throw RuntimeFailures.domainViolation(
                        DiagnosticCode.RUNTIME_ROOT_COMPLEX_DOMAIN,
                        "negative radicand with a reduced degree numerator that is even requires a complex result",
                        sourceSpan);
            }
            negativeResult = degreeParity.denominatorOdd();
        }
        MathContext magnitudeContext = negativeResult ? swapDirectionalRounding(mathContext) : mathContext;
        BigDecimal magnitude = rootMagnitude(adapter, radicand.abs(), degree, magnitudeContext, sourceSpan);
        return negativeResult ? magnitude.negate() : magnitude;
    }

    private static BigDecimal rootMagnitude(
            BigMathAdapter adapter,
            BigDecimal absRadicand,
            BigDecimal degree,
            MathContext magnitudeContext,
            SourceSpan sourceSpan) {
        try {
            if (degree.signum() < 0) {
                BigDecimal reciprocalRoot = adapter.root(absRadicand, degree.abs(), magnitudeContext);
                return adapter.reciprocal(reciprocalRoot, magnitudeContext);
            }
            return adapter.root(absRadicand, degree, magnitudeContext);
        } catch (ArithmeticException exception) {
            throw RuntimeFailures.calculationFailure("root calculation failed", sourceSpan, exception);
        }
    }

    private static BigDecimal callPow(
            BigMathAdapter adapter,
            BigDecimal base,
            BigDecimal exponent,
            MathContext mathContext,
            SourceSpan sourceSpan) {
        try {
            return adapter.pow(base, exponent, mathContext);
        } catch (ArithmeticException exception) {
            throw RuntimeFailures.calculationFailure("power calculation failed", sourceSpan, exception);
        }
    }

    private static MathContext swapDirectionalRounding(MathContext mathContext) {
        RoundingMode roundingMode = mathContext.getRoundingMode();
        RoundingMode swapped = switch (roundingMode) {
            case CEILING -> RoundingMode.FLOOR;
            case FLOOR -> RoundingMode.CEILING;
            default -> roundingMode;
        };
        return swapped == roundingMode ? mathContext : new MathContext(mathContext.getPrecision(), swapped);
    }
}
