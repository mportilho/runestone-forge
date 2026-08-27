package com.runestone.expeval_mk3.perf.jmh;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CalculationMemoryStoragePrototypeTest {

    @Test
    void allStoragePublicationAndReachabilityCombinationsProduceTheSameContract() {
        int[] slotCounts = {0, 1, 4, 16, 64, 256};

        for (int slotCount : slotCounts) {
            for (CalculationMemoryStoragePrototypeBenchmark.Reachability reachability
                    : CalculationMemoryStoragePrototypeBenchmark.Reachability.values()) {
                var state = state(slotCount, reachability);
                var columnar = state.columnarMemory();

                assertThat(columnar.indexedChecksum()).isEqualTo(columnar.listChecksum());
                assertThat(columnar.variables()).hasSize(columnar.variableCount());
                assertThat(columnar.calculations()).hasSize(columnar.calculationCount());
                assertThat(state.assignmentsSchema().calculationKeys())
                        .isSameAs(state.fullSchema().calculationKeys());
            }
        }
    }

    @Test
    void projectedListsAreImmutableAndIndexedBoundsMatchListBounds() {
        var memory = state(4, CalculationMemoryStoragePrototypeBenchmark.Reachability.DENSE)
                .columnarMemory();

        assertThatThrownBy(() -> memory.variables().add(null))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> memory.calculations().add(null))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> memory.variableKeyAt(memory.variableCount()))
                .isInstanceOf(IndexOutOfBoundsException.class);
        assertThatThrownBy(() -> memory.calculationValueAt(memory.calculationCount()))
                .isInstanceOf(IndexOutOfBoundsException.class);
    }

    @Test
    void representativeEtapa10ShapesProduceEquivalentPayloads() {
        for (CalculationMemoryStoragePrototypeBenchmark.RepresentativeShape shape
                : CalculationMemoryStoragePrototypeBenchmark.RepresentativeShape.values()) {
            var state = new CalculationMemoryStoragePrototypeBenchmark.RepresentativeState();
            state.configure(shape);
            state.setUp();

            assertThat(state.columnarMemory().indexedChecksum())
                    .as(shape.name())
                    .isEqualTo(state.appendColumnarMemory().indexedChecksum())
                    .isEqualTo(state.columnarMemory().listChecksum());

            var append = state.binding().captureAppend();
            assertThat(append.count()).as(shape.name()).isEqualTo(shape.reachedCount());
            if (append.ordinals() != null) {
                assertThat(append.ordinals())
                        .as(shape.name())
                        .startsWith(shape.reachedSlots());
            }
        }
    }

    private static CalculationMemoryStoragePrototypeBenchmark.BindingState state(
            int slotCount,
            CalculationMemoryStoragePrototypeBenchmark.Reachability reachability) {
        var state = new CalculationMemoryStoragePrototypeBenchmark.BindingState();
        state.configure(slotCount, reachability);
        state.setUp();
        return state;
    }
}
