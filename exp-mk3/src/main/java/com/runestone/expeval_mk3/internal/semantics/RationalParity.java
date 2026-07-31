package com.runestone.expeval_mk3.internal.semantics;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

/**
 * The exact canonical reduced rational {@code p/q} sign and parity of a finite decimal, per ADR 0017.
 * Classification never materializes a denominator proportional to {@code 10^scale}: parity is derived
 * from the 2-adic valuation of the unscaled value compared against the decimal scale.
 */
public record RationalParity(int signum, boolean numeratorOdd, boolean denominatorOdd) {

    private static final RationalParity ZERO = new RationalParity(0, false, true);

    public RationalParity {
        if (signum < -1 || signum > 1) {
            throw new IllegalArgumentException("signum must be -1, 0, or 1");
        }
        if (signum == 0 && numeratorOdd) {
            throw new IllegalArgumentException("zero numerator is never odd");
        }
    }

    public static RationalParity classify(BigDecimal value) {
        Objects.requireNonNull(value, "value");
        BigInteger unscaled = value.unscaledValue();
        if (unscaled.signum() == 0) {
            return ZERO;
        }
        int scale = value.scale();
        int twoAdicValuation = unscaled.abs().getLowestSetBit();
        boolean denominatorOdd = twoAdicValuation >= scale;
        boolean numeratorOdd = twoAdicValuation <= scale;
        return new RationalParity(unscaled.signum(), numeratorOdd, denominatorOdd);
    }

    public RationalParity negate() {
        return new RationalParity(-signum, numeratorOdd, denominatorOdd);
    }
}
