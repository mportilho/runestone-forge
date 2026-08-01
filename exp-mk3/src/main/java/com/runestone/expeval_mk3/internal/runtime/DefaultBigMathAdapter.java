package com.runestone.expeval_mk3.internal.runtime;

import ch.obermuhlner.math.big.BigDecimalMath;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * The production {@link BigMathAdapter}: a direct, unconditional delegation to {@code BigDecimalMath}.
 * {@link RealDomainArithmetic} is the only caller that decides what arguments reach this seam.
 */
final class DefaultBigMathAdapter implements BigMathAdapter {

    @Override
    public BigDecimal pow(BigDecimal base, BigDecimal exponent, MathContext mathContext) {
        return BigDecimalMath.pow(base, exponent, mathContext);
    }

    @Override
    public BigDecimal root(BigDecimal radicand, BigDecimal degree, MathContext mathContext) {
        return BigDecimalMath.root(radicand, degree, mathContext);
    }

    @Override
    public BigDecimal reciprocal(BigDecimal value, MathContext mathContext) {
        return BigDecimalMath.reciprocal(value, mathContext);
    }
}
