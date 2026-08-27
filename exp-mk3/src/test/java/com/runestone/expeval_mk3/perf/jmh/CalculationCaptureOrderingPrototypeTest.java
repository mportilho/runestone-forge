package com.runestone.expeval_mk3.perf.jmh;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CalculationCaptureOrderingPrototypeTest {

    @Test
    void requiredSemanticFlowsProduceMonotonicReachedOrdinalsAndEquivalentPayloads() {
        for (Scenario scenario : Scenario.values()) {
            Probe probe = new Probe();
            scenario.execute(probe);
            int[] reached = probe.reached.stream().mapToInt(Integer::intValue).toArray();

            assertThat(reached).as(scenario.name()).isSorted();
            assertThat(reached).as(scenario.name()).doesNotHaveDuplicates();
            assertThat(probe.providerInvocations).as(scenario.name()).isEqualTo(scenario.providerInvocations);

            var state = new CalculationMemoryStoragePrototypeBenchmark.BindingState();
            state.configure(8, 4, scenario.slotCount, reached);
            state.setUp();
            assertThat(state.appendColumnarMemory().indexedChecksum())
                    .as(scenario.name())
                    .isEqualTo(state.columnarMemory().indexedChecksum());
        }
    }

    private enum Scenario {
        EMPTY(0, 0) {
            @Override
            void execute(Probe probe) {
            }
        },
        MARKABLE_ROOT(1, 1) {
            @Override
            void execute(Probe probe) {
                probe.provider(0);
            }
        },
        NESTED_FUNCTIONS(2, 2) {
            @Override
            void execute(Probe probe) {
                probe.provider(0);
                probe.provider(1);
            }
        },
        ASSIGNMENT_THEN_RESULT(2, 2) {
            @Override
            void execute(Probe probe) {
                probe.provider(0);
                probe.provider(1);
            }
        },
        SHORT_CIRCUIT(3, 2) {
            @Override
            void execute(Probe probe) {
                boolean left = probe.provider(0) != null;
                if (!left) {
                    probe.provider(1);
                }
                probe.provider(2);
            }
        },
        SELECTED_CONDITIONAL(4, 3) {
            @Override
            void execute(Probe probe) {
                boolean condition = probe.provider(0) != null;
                probe.provider(condition ? 1 : 2);
                probe.provider(3);
            }
        },
        NULL_COALESCE(3, 2) {
            @Override
            void execute(Probe probe) {
                Object selected = probe.provider(0);
                if (selected == null) {
                    probe.provider(1);
                }
                probe.provider(2);
            }
        },
        FOLDED_PROVENANCE_GROUP(3, 0) {
            @Override
            void execute(Probe probe) {
                probe.replay(0, 1, 2);
            }
        },
        CSE_MISS_THEN_HIT(2, 1) {
            @Override
            void execute(Probe probe) {
                probe.invokeProvider();
                probe.replay(0);
                probe.replay(1);
            }
        },
        OPAQUE_COLLECTION(2, 5) {
            @Override
            void execute(Probe probe) {
                probe.provider(0);
                for (int index = 0; index < 3; index++) {
                    probe.invokeProvider();
                }
                probe.provider(1);
            }
        };

        private final int slotCount;
        private final int providerInvocations;

        Scenario(int slotCount, int providerInvocations) {
            this.slotCount = slotCount;
            this.providerInvocations = providerInvocations;
        }

        abstract void execute(Probe probe);
    }

    private static final class Probe {

        private final List<Integer> reached = new ArrayList<>();
        private int providerInvocations;

        Object provider(int ordinal) {
            providerInvocations++;
            reached.add(ordinal);
            return ordinal == 0 ? new Object() : null;
        }

        void invokeProvider() {
            providerInvocations++;
        }

        void replay(int... ordinals) {
            for (int ordinal : ordinals) {
                reached.add(ordinal);
            }
        }
    }
}
