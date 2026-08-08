package com.runestone.expeval_mk3.internal.plan;

import java.util.Objects;

/**
 * Wraps a reference so a {@link StructuralKey} compares it by identity regardless of whether the
 * wrapped type overrides {@code equals}/{@code hashCode} structurally. Subexpressao Comum Memoizada
 * (issue #121) requires identity comparison for a {@code FunctionDescriptor}, a compiled regex
 * {@code Pattern}, and the identity-bearing accessor of a navigation binding, because two occurrences
 * resolved independently can wrap the very same catalog-cached instance even when the surrounding
 * binding record is freshly allocated per occurrence.
 */
final class IdentityKey {

    private final Object value;

    IdentityKey(Object value) {
        this.value = Objects.requireNonNull(value, "value");
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof IdentityKey identityKey && identityKey.value == value;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(value);
    }
}
