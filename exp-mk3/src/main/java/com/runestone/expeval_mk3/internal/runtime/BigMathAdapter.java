package com.runestone.expeval_mk3.internal.runtime;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * The only seam through which {@link RealDomainArithmetic} reaches {@code big-math}: every power and
 * root magnitude, integral or fractional, is calculated through one of these three calls. Tests observe
 * this seam directly to prove the ADR 0017 adapters never submit a negative base to {@code pow} nor a
 * negative degree to {@code root}.
 */
interface BigMathAdapter {

    BigMathAdapter DEFAULT = new DefaultBigMathAdapter();

    BigDecimal pow(BigDecimal base, BigDecimal exponent, MathContext mathContext);

    BigDecimal root(BigDecimal radicand, BigDecimal degree, MathContext mathContext);

    BigDecimal reciprocal(BigDecimal value, MathContext mathContext);
}
