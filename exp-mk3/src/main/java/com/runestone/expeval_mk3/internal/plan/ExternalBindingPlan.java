package com.runestone.expeval_mk3.internal.plan;

import com.runestone.expeval_mk3.api.ExternalSymbol;

import java.util.Objects;

record ExternalBindingPlan(ExternalSymbol symbol, int frameSlot) {

    ExternalBindingPlan {
        Objects.requireNonNull(symbol, "symbol");
    }
}
