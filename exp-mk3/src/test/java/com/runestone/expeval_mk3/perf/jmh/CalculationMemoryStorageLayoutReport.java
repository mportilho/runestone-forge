package com.runestone.expeval_mk3.perf.jmh;

import org.openjdk.jol.info.GraphLayout;

/** Prints retained graph sizes for the binding benchmark's publication candidates. */
public final class CalculationMemoryStorageLayoutReport {

    private CalculationMemoryStorageLayoutReport() {
    }

    public static void main(String[] args) {
        System.out.println("slots,reachability,columnarBytes,eagerBytes");
        int[] slotCounts = {0, 1, 4, 16, 64, 256};
        for (int slotCount : slotCounts) {
            for (CalculationMemoryStoragePrototypeBenchmark.Reachability reachability
                    : CalculationMemoryStoragePrototypeBenchmark.Reachability.values()) {
                var state = new CalculationMemoryStoragePrototypeBenchmark.BindingState();
                state.configure(slotCount, reachability);
                state.setUp();
                long columnarBytes = GraphLayout.parseInstance(state.columnarMemory()).totalSize();
                long eagerBytes = GraphLayout.parseInstance(state.eagerMemory()).totalSize();
                System.out.printf(
                        "%d,%s,%d,%d%n",
                        slotCount,
                        reachability,
                        columnarBytes,
                        eagerBytes);
            }
        }
    }
}
