package com.runestone.expeval_mk3.internal.plan;

import com.runestone.expeval_mk3.api.ExpressionType;
import com.runestone.expeval_mk3.api.SourceSpan;

import java.util.Objects;

/**
 * One internal symbol reachable through the assignments view: its public type, the stable frame slot
 * holding its final value, and the source position of its first creation for view-rejection diagnostics.
 */
public record AssignedSymbol(String name, ExpressionType type, int frameSlot, SourceSpan sourceSpan) {

    public AssignedSymbol {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        if (name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (frameSlot < 0) {
            throw new IllegalArgumentException("frameSlot must not be negative");
        }
    }
}
