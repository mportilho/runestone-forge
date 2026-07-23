package com.runestone.expeval_mk3.api;

import java.util.Objects;

/**
 * The sole sequential expression type, represented canonically as an immutable ordered list.
 */
public record CollectionType(ExpressionType elementType) implements ExpressionType {

    public CollectionType {
        Objects.requireNonNull(elementType, "elementType");
    }
}
