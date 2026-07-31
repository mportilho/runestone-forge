package com.runestone.expeval_mk3.internal.semantics;

import com.runestone.expeval_mk3.api.ExpressionType;
import com.runestone.expeval_mk3.api.RuntimeNullability;

import java.util.Objects;

public record IndexSubscriptNavigationBinding(
        ExpressionType elementType,
        RuntimeNullability resultNullability,
        boolean pure) implements NavigationBinding {

    public IndexSubscriptNavigationBinding {
        Objects.requireNonNull(elementType, "elementType");
        Objects.requireNonNull(resultNullability, "resultNullability");
    }
}
