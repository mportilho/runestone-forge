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
        return normalizedSliceBound(rawValue(bound), size, unboundedValue);
    }

    /**
     * Extracts the raw bound value ({@code null} for the unbounded case) out of the AST bound shape.
     * Execution plan nodes use this once at build time to avoid retaining {@link SubscriptSliceBound}
     * itself, which is an AST type.
     */
    public static BigInteger rawValue(SubscriptSliceBound bound) {
        Objects.requireNonNull(bound, "bound");
        return bound == UnboundedSubscriptSliceBound.INSTANCE
                ? null
                : ((IntegerSubscriptSliceBound) bound).integer().value();
    }

    /**
     * Same normalization as {@link #normalizedSliceBound(SubscriptSliceBound, int, int)}, but takes the
     * already-extracted raw bound value ({@code null} for the unbounded case) instead of the AST bound
     * shape, so callers that must not retain AST nodes (execution plan nodes) can still normalize.
     */
    public static int normalizedSliceBound(BigInteger rawValue, int size, int unboundedValue) {
        if (rawValue == null) {
            return unboundedValue;
        }
        BigInteger normalized = normalizedIndexValue(rawValue, size);
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
