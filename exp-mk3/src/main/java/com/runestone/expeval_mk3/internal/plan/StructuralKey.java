package com.runestone.expeval_mk3.internal.plan;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * The canonical identity of a subtree for Subexpressao Comum Memoizada (issue #121): two occurrences
 * share a {@code StructuralKey} exactly when {@link CommonSubexpressionAnalyzer} built them from the
 * same operator/node kind, the same children keys, and the same identity-bearing bindings (wrapped in
 * {@link IdentityKey} by the analyzer, never compared structurally). {@code NodeId} never takes part:
 * it is unique per node by construction and would defeat every comparison. Parts may contain
 * {@code null} (e.g. an unbounded slice bound), so the list is a plain unmodifiable view rather than
 * {@code List.copyOf}, which rejects null elements.
 */
record StructuralKey(String kind, List<Object> parts) {

    StructuralKey {
        Objects.requireNonNull(kind, "kind");
        parts = Collections.unmodifiableList(new ArrayList<>(parts));
    }

    static StructuralKey of(String kind, Object... parts) {
        return new StructuralKey(kind, Arrays.asList(parts));
    }
}
