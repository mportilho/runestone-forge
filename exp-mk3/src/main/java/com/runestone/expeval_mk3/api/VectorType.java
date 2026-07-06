package com.runestone.expeval_mk3.api;

import java.util.Objects;

/**
 * Tipo Vetor: vector-literal semantics with preserved element type.
 */
public record VectorType(ExpressionType elementType) implements ExpressionType {

    public VectorType {
        Objects.requireNonNull(elementType, "elementType");
    }
}
