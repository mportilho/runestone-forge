package com.runestone.expeval_mk3.internal.semantics;

public record CollectionShape(int fixedSize) {

    public CollectionShape {
        if (fixedSize < 0) {
            throw new IllegalArgumentException("fixedSize must not be negative");
        }
    }
}
