package com.runestone.expeval.internal.memory;

import com.runestone.expeval.api.CalculationKey;
import com.runestone.expeval.api.CalculationKind;
import com.runestone.expeval.api.SourceSpan;
import com.runestone.expeval.api.VariableKey;
import com.runestone.expeval.api.VariableOrigin;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class CalculationMemorySchemaRetainedWeightTest {

    @Test
    void retainedWeightGrowsWithTheSchemasRetainedVariableAndCalculationMetadata() {
        CalculationMemorySchema small = schema(1);
        CalculationMemorySchema large = schema(32);

        assertThat(large.estimatedRetainedWeight()).isGreaterThan(small.estimatedRetainedWeight());
    }

    @Test
    void retainedWeightIncludesSourceControlledInternalVariableNamesButNotExternalEnvironmentNames() {
        assertThat(variableSchema("internal", VariableOrigin.INTERNAL).estimatedRetainedWeight())
                .isLessThan(variableSchema("internal".repeat(64), VariableOrigin.INTERNAL).estimatedRetainedWeight());
        assertThat(variableSchema("external", VariableOrigin.EXTERNAL).estimatedRetainedWeight())
                .isEqualTo(variableSchema("external".repeat(64), VariableOrigin.EXTERNAL).estimatedRetainedWeight());
    }

    private static CalculationMemorySchema schema(int size) {
        VariableKey[] variables = IntStream.range(0, size)
                .mapToObj(index -> new VariableKey("value" + index, VariableOrigin.INTERNAL))
                .toArray(VariableKey[]::new);
        int[] frameSlots = IntStream.range(0, size).toArray();
        var calculationKeys = IntStream.range(0, size)
                .mapToObj(index -> new CalculationKey(
                        index, new SourceSpan(index, index + 1, 1, index + 1), CalculationKind.FUNCTION, "fn" + index))
                .toList();
        return new CalculationMemorySchema(new VariableMemorySchema(variables, frameSlots), calculationKeys);
    }

    private static VariableMemorySchema variableSchema(String name, VariableOrigin origin) {
        return new VariableMemorySchema(new VariableKey[] {new VariableKey(name, origin)}, new int[] {0});
    }
}
