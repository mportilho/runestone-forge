package com.runestone.expeval_mk3.internal.semantics;

import com.runestone.expeval_mk3.api.ExpressionType;
import com.runestone.expeval_mk3.api.RuntimeNullability;

import java.util.Objects;

public record SliceSubscriptNavigationBinding(
        ExpressionType elementType,
        RuntimeNullability resultNullability,
        boolean pure) implements NavigationBinding {

    public SliceSubscriptNavigationBinding {
        Objects.requireNonNull(elementType, "elementType");
        Objects.requireNonNull(resultNullability, "resultNullability");
    }
}
