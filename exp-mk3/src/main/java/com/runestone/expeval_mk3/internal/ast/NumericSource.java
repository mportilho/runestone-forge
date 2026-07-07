package com.runestone.expeval_mk3.internal.ast;

import java.util.Objects;

public record NumericSource(NodeId nodeId, NumericSourceKind kind) {

    public NumericSource {
        Objects.requireNonNull(nodeId, "nodeId");
        Objects.requireNonNull(kind, "kind");
    }
}
