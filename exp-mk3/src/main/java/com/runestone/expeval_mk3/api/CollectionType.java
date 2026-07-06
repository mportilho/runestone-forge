package com.runestone.expeval_mk3.api;

import java.util.Objects;

/**
 * Tipo Colecao: externally supplied or navigated collections with preserved element type.
 */
public record CollectionType(ExpressionType elementType) implements ExpressionType {

    public CollectionType {
        Objects.requireNonNull(elementType, "elementType");
    }
}
