package com.runestone.expeval.internal.semantics;

import com.runestone.expeval.api.ExpressionType;
import com.runestone.expeval.api.RuntimeNullability;

import java.util.Objects;

public record MapKeySubscriptNavigationBinding(
        ExpressionType valueType,
        RuntimeNullability resultNullability,
        boolean pure) implements NavigationBinding {

    public MapKeySubscriptNavigationBinding {
        Objects.requireNonNull(valueType, "valueType");
        Objects.requireNonNull(resultNullability, "resultNullability");
    }
}
