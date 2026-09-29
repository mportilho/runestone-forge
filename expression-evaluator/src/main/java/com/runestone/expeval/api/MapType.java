package com.runestone.expeval.api;

import java.util.Objects;

/**
 * Tipo Mapa: a text-keyed map with preserved value type.
 */
public record MapType(ExpressionType valueType) implements ExpressionType {

    public MapType {
        Objects.requireNonNull(valueType, "valueType");
    }
}
