package com.runestone.expeval_mk3.api;

import java.util.Objects;

/**
 * Nominal Tipo Objeto. Object identity is the type name, not structural members.
 */
public record ObjectType(String name) implements ExpressionType {

    public ObjectType {
        Objects.requireNonNull(name, "name");
        if (name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
    }
}
