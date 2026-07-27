package com.runestone.expeval_mk3.internal.ast;

import java.math.BigInteger;
import java.util.Objects;

public final class SubscriptBounds {

    private SubscriptBounds() {
    }

    public static boolean indexWithinFixedSize(BigInteger rawIndex, int size) {
        BigInteger normalized = normalizedIndexValue(rawIndex, size);
        return normalized.signum() >= 0 && normalized.compareTo(BigInteger.valueOf(size)) < 0;
    }

    public static int normalizedIndex(BigInteger rawIndex, int size) {
        BigInteger normalized = normalizedIndexValue(rawIndex, size);
        if (normalized.signum() < 0 || normalized.compareTo(BigInteger.valueOf(size)) >= 0) {
            throw new IndexOutOfBoundsException("collection index out of bounds: " + rawIndex);
        }
        try {
            return normalized.intValueExact();
        } catch (ArithmeticException exception) {
            throw new IndexOutOfBoundsException("collection index out of bounds: " + rawIndex);
        }
    }

    public static int normalizedSliceBound(SubscriptSliceBound bound, int size, int unboundedValue) {
        Objects.requireNonNull(bound, "bound");
        if (bound == UnboundedSubscriptSliceBound.INSTANCE) {
            return unboundedValue;
        }
        BigInteger raw = ((IntegerSubscriptSliceBound) bound).integer().value();
        BigInteger normalized = normalizedIndexValue(raw, size);
        if (normalized.signum() < 0) {
            return 0;
        }
        BigInteger max = BigInteger.valueOf(size);
        if (normalized.compareTo(max) > 0) {
            return size;
        }
        return normalized.intValue();
    }

    private static BigInteger normalizedIndexValue(BigInteger rawIndex, int size) {
        Objects.requireNonNull(rawIndex, "rawIndex");
        if (size < 0) {
            throw new IllegalArgumentException("size must not be negative");
        }
        return rawIndex.signum() < 0 ? rawIndex.add(BigInteger.valueOf(size)) : rawIndex;
    }
}
