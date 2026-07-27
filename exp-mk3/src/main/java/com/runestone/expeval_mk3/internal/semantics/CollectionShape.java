package com.runestone.expeval_mk3.internal.semantics;

public record CollectionShape(boolean fixed, int fixedSize) {

    public static CollectionShape unknown() {
        return new CollectionShape(false, 0);
    }

    public CollectionShape(int fixedSize) {
        this(true, fixedSize);
    }

    public CollectionShape {
        if (fixedSize < 0) {
            throw new IllegalArgumentException("fixedSize must not be negative");
        }
        if (!fixed && fixedSize != 0) {
            throw new IllegalArgumentException("unknown collection shape must not declare a fixed size");
        }
    }
}
