package com.runestone.expeval.internal.semantics;

import com.runestone.expeval.api.ExpressionType;
import com.runestone.expeval.api.RuntimeNullability;

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
