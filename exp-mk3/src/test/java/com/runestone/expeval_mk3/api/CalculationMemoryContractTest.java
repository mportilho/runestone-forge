package com.runestone.expeval_mk3.api;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CalculationMemoryContractTest {

    private static final SourceSpan SPAN = new SourceSpan(0, 1, 1, 1);

    @Test
    void publicValueTypesRejectInvalidConstruction() {
        assertThatNullPointerException().isThrownBy(() -> new ComputationWithMemory<>(null, CalculationMemory.empty()));
        assertThatNullPointerException().isThrownBy(() -> new ComputationWithMemory<>("value", null));
        assertThatThrownBy(() -> new VariableKey(" ", VariableOrigin.EXTERNAL))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatNullPointerException().isThrownBy(() -> new VariableEntry(null, "value"));
        assertThatNullPointerException().isThrownBy(() -> new VariableEntry(
                new VariableKey("x", VariableOrigin.INTERNAL), null));
        assertThatThrownBy(() -> new CalculationKey(-1, SPAN, CalculationKind.FUNCTION, "f"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatNullPointerException().isThrownBy(() -> new CalculationEntry(null, null));
    }

}
