package com.runestone.expeval_mk3.internal.semantics;

import com.runestone.expeval_mk3.api.ExpressionType;
import com.runestone.expeval_mk3.api.RuntimeNullability;

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
