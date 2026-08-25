package com.runestone.expeval_mk3.internal.memory;

import com.runestone.expeval_mk3.api.CalculationEntry;
import com.runestone.expeval_mk3.api.CalculationKey;
import com.runestone.expeval_mk3.api.CalculationKind;
import com.runestone.expeval_mk3.api.SourceSpan;
import com.runestone.expeval_mk3.api.VariableKey;
import com.runestone.expeval_mk3.api.VariableOrigin;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DefaultCalculationMemoryTest {

    private static final SourceSpan SPAN = new SourceSpan(0, 1, 1, 1);

    @Test
    void acceptsReachedNullCalculationsAndKeepsImmutableSchemaReferences() {
        VariableKey variableKey = new VariableKey("x", VariableOrigin.EXTERNAL);
        CalculationKey calculationKey = new CalculationKey(1, SPAN, CalculationKind.FUNCTION, "f");
        DefaultCalculationMemory memory = new DefaultCalculationMemory(
                List.of(variableKey), new Object[] {"value"}, List.of(calculationKey), new Object[] {null}, null);

        assertThat(memory.variableKeyAt(0)).isSameAs(variableKey);
        assertThat(memory.calculations()).containsExactly(new CalculationEntry(calculationKey, null));
    }

    @Test
    void rejectsMismatchedColumnsAndInvalidOrdinals() {
        VariableKey variableKey = new VariableKey("x", VariableOrigin.EXTERNAL);
        CalculationKey calculationKey = new CalculationKey(1, SPAN, CalculationKind.FUNCTION, "f");

        assertThatThrownBy(() -> new DefaultCalculationMemory(
                List.of(variableKey), new Object[0], List.of(), new Object[0], null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new DefaultCalculationMemory(
                List.of(), new Object[0], List.of(calculationKey), new Object[] {"x"}, new int[] {1}))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
