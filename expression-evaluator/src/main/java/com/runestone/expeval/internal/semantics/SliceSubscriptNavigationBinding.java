package com.runestone.expeval.internal.semantics;

import com.runestone.expeval.api.ExpressionType;
import com.runestone.expeval.api.RuntimeNullability;

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
