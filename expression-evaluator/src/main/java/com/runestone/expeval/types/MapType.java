package com.runestone.expeval.types;

import java.util.Objects;

/**
 * Parametrized map type — represents a {@code Map<K, V>} where both the key type {@code K} and the
 * value type {@code V} are known at compile time.
 *
 * <p>Map navigation uses this type to propagate precise type information through steps such as
 * {@code ["key"]}, {@code [*]}, {@code [?(@.key =~ "...")]} and {@code ..keys()} / {@code ..values()}.
 */
public record MapType(ResolvedType keyType, ResolvedType valueType) implements ResolvedType {

    public MapType {
        Objects.requireNonNull(keyType, "keyType must not be null");
        Objects.requireNonNull(valueType, "valueType must not be null");
    }
}
