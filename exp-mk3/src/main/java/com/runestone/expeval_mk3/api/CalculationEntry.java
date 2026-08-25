package com.runestone.expeval_mk3.api;

import java.util.Objects;

public record CalculationEntry(CalculationKey key, Object value) {

    public CalculationEntry {
        Objects.requireNonNull(key, "key");
    }
}
