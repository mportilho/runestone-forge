package com.runestone.expeval_mk3.internal.runtime;

import com.runestone.expeval_mk3.api.SourceSpan;
import com.runestone.expeval_mk3.internal.ast.NodeId;

import java.util.Objects;

/** Reads a symbol or current-item binding's frame slot; both resolve to the same frame-read shape. */
public record FrameReadExecutableNode(NodeId id, SourceSpan sourceSpan, int frameSlot) implements ExecutableNode {

    public FrameReadExecutableNode {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        if (frameSlot < 0) {
            throw new IllegalArgumentException("frameSlot must not be negative");
        }
    }

    @Override
    public Object execute(ExecutionScope scope) {
        return scope.read(frameSlot);
    }
}
