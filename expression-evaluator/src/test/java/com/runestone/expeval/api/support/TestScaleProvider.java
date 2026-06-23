package com.runestone.expeval.api.support;

import java.math.BigDecimal;
import java.util.List;

/**
 * Public top-level instance provider for collection-function tests.
 * Must be public so that {@link java.lang.invoke.MethodHandles#lookup()} from
 * inside the library can create method handles for its instance methods.
 */
public final class TestScaleProvider {

    private final BigDecimal factor;

    public TestScaleProvider(BigDecimal factor) {
        this.factor = factor;
    }

    /**
     * {@code ..scaleAndSum()} — multiplies every element by the instance's factor
     * and returns the total.
     */
    public BigDecimal scaleAndSum(List<BigDecimal> list) {
        return list.stream()
                .map(v -> v.multiply(factor))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
