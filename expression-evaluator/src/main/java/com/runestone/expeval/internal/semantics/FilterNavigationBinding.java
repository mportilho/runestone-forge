package com.runestone.expeval.internal.semantics;

import com.runestone.expeval.api.ExpressionType;
import com.runestone.expeval.api.RuntimeNullability;

import java.util.Objects;

public record FilterNavigationBinding(
        ExpressionType elementType,
        RuntimeNullability resultNullability,
        int currentItemFrameSlot,
        boolean pure) implements NavigationBinding {

    public FilterNavigationBinding {
        Objects.requireNonNull(elementType, "elementType");
        Objects.requireNonNull(resultNullability, "resultNullability");
        if (currentItemFrameSlot < 0) {
            throw new IllegalArgumentException("currentItemFrameSlot must not be negative");
        }
    }
}
