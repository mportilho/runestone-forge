package com.runestone.expeval_mk3.internal.ast;

import java.util.List;
import java.util.Objects;

public record MethodNavigationSignature(String memberName, List<NodeId> argumentNodeIds) {

    public MethodNavigationSignature {
        memberName = Objects.requireNonNull(memberName, "memberName");
        if (memberName.isBlank()) {
            throw new IllegalArgumentException("memberName must not be blank");
        }
        Objects.requireNonNull(argumentNodeIds, "argumentNodeIds");
        argumentNodeIds = List.copyOf(argumentNodeIds);
    }
}
