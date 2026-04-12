package com.runestone.expeval.types;

import java.util.Objects;

/**
 * Parametrized collection type — represents a {@code Collection<E>} or {@code List<E>} where the
 * element type {@code E} is known at compile time.
 *
 * <p>Distinct from {@link VectorType#INSTANCE}, which is a non-parametrized singleton used when the
 * element type is unknown. Using {@code CollectionType} preserves element-type information through
 * navigation steps such as {@code [*]}, {@code [n]}, and {@code [?(...)]}.
 */
public record CollectionType(ResolvedType elementType) implements ResolvedType {

    public CollectionType {
        Objects.requireNonNull(elementType, "elementType must not be null");
    }
}
