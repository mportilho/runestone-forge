package com.runestone.expeval_mk3.internal.memory;

import com.runestone.expeval_mk3.api.CalculationKey;
import com.runestone.expeval_mk3.api.CalculationKind;
import com.runestone.expeval_mk3.api.SourceSpan;
import com.runestone.expeval_mk3.internal.runtime.ExecutionScope;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CalculationRecorderTest {

    private static final SourceSpan SPAN = new SourceSpan(0, 1, 1, 1);

    @Test
    void freezesDenseAndGappedReachWithoutLosingReachedNull() {
        CalculationMemorySchema schema = schema(3);
        ExecutionScope scope = new ExecutionScope(new Object[0], ZoneOffset.UTC, Clock.systemUTC());
        CalculationRecorder dense = schema.newRecorder();
        dense.append(0, "first");
        dense.append(1, null);
        CalculationRecorder gapped = schema.newRecorder();
        gapped.append(1, "second");

        var denseMemory = schema.freeze(scope, dense);
        var gappedMemory = schema.freeze(scope, gapped);

        assertThat(denseMemory.calculationCount()).isEqualTo(2);
        assertThat(denseMemory.calculationValueAt(1)).isNull();
        assertThat(denseMemory.calculationKeyAt(1).name()).isEqualTo("point-1");
        assertThatThrownBy(() -> denseMemory.calculationKeyAt(2)).isInstanceOf(IndexOutOfBoundsException.class);
        assertThat(gappedMemory.calculationCount()).isOne();
        assertThat(gappedMemory.calculationKeyAt(0).name()).isEqualTo("point-1");
    }

    @Test
    void rejectsDuplicateDecreasingAndOutOfSchemaOrdinals() {
        CalculationRecorder recorder = schema(3).newRecorder();
        recorder.append(1, "value");

        assertThatThrownBy(() -> recorder.append(1, "duplicate"))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> recorder.append(0, "decreasing"))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> recorder.append(3, "outside"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void schemaWithoutCalculationPointsCreatesNoRecorderAndFreezesToSharedEmptyMemory() {
        CalculationMemorySchema schema = schema(0);
        ExecutionScope scope = new ExecutionScope(new Object[0], ZoneOffset.UTC, Clock.systemUTC());

        assertThat(schema.newRecorder()).isNull();
        assertThat(schema.freeze(scope, null)).isSameAs(DefaultCalculationMemory.emptyInstance());
    }

    private static CalculationMemorySchema schema(int pointCount) {
        List<CalculationKey> keys = java.util.stream.IntStream.range(0, pointCount)
                .mapToObj(index -> new CalculationKey(index, SPAN, CalculationKind.FUNCTION, "point-" + index))
                .toList();
        return new CalculationMemorySchema(new VariableMemorySchema(new com.runestone.expeval_mk3.api.VariableKey[0], new int[0]), keys);
    }
}
