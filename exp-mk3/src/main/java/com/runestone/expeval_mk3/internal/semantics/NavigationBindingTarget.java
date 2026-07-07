package com.runestone.expeval_mk3.internal.semantics;

import com.runestone.expeval_mk3.api.ExpressionType;

import java.util.Objects;

public record NavigationBindingTarget(ExpressionType receiverType, ExpressionType resultType) {

    public NavigationBindingTarget {
        Objects.requireNonNull(receiverType, "receiverType");
        Objects.requireNonNull(resultType, "resultType");
    }
}
