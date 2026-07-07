package com.runestone.expeval_mk3.internal.ast;

import java.time.OffsetDateTime;
import java.util.Objects;

public record OffsetDateTimeLiteralSource(NodeId nodeId, OffsetDateTime value) {

    public OffsetDateTimeLiteralSource {
        Objects.requireNonNull(nodeId, "nodeId");
        Objects.requireNonNull(value, "value");
    }
}
