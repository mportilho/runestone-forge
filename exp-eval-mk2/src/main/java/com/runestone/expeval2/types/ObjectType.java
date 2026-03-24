package com.runestone.expeval2.types;

import java.util.Objects;

public record ObjectType(Class<?> javaClass) implements ResolvedType {

    public ObjectType {
        Objects.requireNonNull(javaClass, "javaClass must not be null");
    }
}
