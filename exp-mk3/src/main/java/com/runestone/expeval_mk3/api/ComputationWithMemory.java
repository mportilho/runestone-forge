package com.runestone.expeval_mk3.api;

import java.util.Objects;

public record ComputationWithMemory<T>(T result, CalculationMemory memory) {

    public ComputationWithMemory {
        Objects.requireNonNull(result, "result");
        Objects.requireNonNull(memory, "memory");
    }
}
