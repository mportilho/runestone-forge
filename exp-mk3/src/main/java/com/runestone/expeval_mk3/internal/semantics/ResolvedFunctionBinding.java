package com.runestone.expeval_mk3.internal.semantics;

import com.runestone.expeval_mk3.api.FunctionDescriptor;
import com.runestone.expeval_mk3.api.UnknownType;
import com.runestone.expeval_mk3.internal.ast.NodeId;

import java.util.List;
import java.util.Objects;

public record ResolvedFunctionBinding(
        FunctionDescriptor descriptor,
        UnknownArgumentHandling unknownArgumentHandling,
        List<NodeId> unknownArgumentNodeIds) {

    public ResolvedFunctionBinding {
        Objects.requireNonNull(descriptor, "descriptor");
        Objects.requireNonNull(unknownArgumentHandling, "unknownArgumentHandling");
        Objects.requireNonNull(unknownArgumentNodeIds, "unknownArgumentNodeIds");
        unknownArgumentNodeIds = List.copyOf(unknownArgumentNodeIds);
        if (unknownArgumentHandling == UnknownArgumentHandling.NONE && !unknownArgumentNodeIds.isEmpty()
                && descriptor.parameterTypes().stream().noneMatch(type -> type == UnknownType.INSTANCE)) {
            throw new IllegalArgumentException("unknown argument nodes without a residual check require unknown-accepting parameters");
        }
    }

    public boolean unknownArgumentCheck() {
        return unknownArgumentHandling == UnknownArgumentHandling.RESIDUAL_CHECK;
    }

    public enum UnknownArgumentHandling {
        NONE,
        RESIDUAL_CHECK
    }
}
