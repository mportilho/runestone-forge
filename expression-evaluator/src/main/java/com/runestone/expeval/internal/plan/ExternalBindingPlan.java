package com.runestone.expeval.internal.plan;

import com.runestone.expeval.api.ExternalSymbol;

import java.util.Objects;

record ExternalBindingPlan(ExternalSymbol symbol, int frameSlot) {

    ExternalBindingPlan {
        Objects.requireNonNull(symbol, "symbol");
    }
}
