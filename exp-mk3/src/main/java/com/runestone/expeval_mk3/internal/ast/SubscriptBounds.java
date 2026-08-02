package com.runestone.expeval_mk3.internal.ast;

import java.math.BigInteger;
import java.util.Objects;

public final class SubscriptBounds {

    /** Sentinel returned by {@link #normalizedIndexOrOutOfBounds}; no valid normalized index is negative. */
    public static final int INDEX_OUT_OF_BOUNDS = -1;

    private SubscriptBounds() {
    }

    public static boolean indexWithinFixedSize(BigInteger rawIndex, int size) {
        BigInteger normalized = normalizedIndexValue(rawIndex, size);
        return normalized.signum() >= 0 && normalized.compareTo(BigInteger.valueOf(size)) < 0;
    }

    /**
     * Normalizes a possibly-negative literal index against {@code size} and returns
     * {@link #INDEX_OUT_OF_BOUNDS} when the normalized index falls outside {@code [0, size)}. Out of
     * range is an ordinary answer here rather than an exception because ADR 0018 makes it a failure
     * only on a strict link; a safe link turns the same answer into a runtime null.
     */
    public static int normalizedIndexOrOutOfBounds(BigInteger rawIndex, int size) {
        BigInteger normalized = normalizedIndexValue(rawIndex, size);
        if (normalized.signum() < 0 || normalized.compareTo(BigInteger.valueOf(size)) >= 0) {
            return INDEX_OUT_OF_BOUNDS;
        }
        // A normalized index below size is always an int; size itself is an int.
        return normalized.intValueExact();
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
