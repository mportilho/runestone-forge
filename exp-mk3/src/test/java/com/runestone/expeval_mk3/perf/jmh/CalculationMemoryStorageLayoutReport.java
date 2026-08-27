package com.runestone.expeval_mk3.perf.jmh;

import org.openjdk.jol.info.GraphLayout;

/** Prints retained graph sizes for the binding benchmark's publication candidates. */
public final class CalculationMemoryStorageLayoutReport {

    private CalculationMemoryStorageLayoutReport() {
    }

    public static void main(String[] args) {
        System.out.println("slots,reachability,frameColumnarBytes,appendColumnarBytes,eagerBytes");
        int[] slotCounts = {0, 1, 4, 16, 64, 256};
        for (int slotCount : slotCounts) {
            for (CalculationMemoryStoragePrototypeBenchmark.Reachability reachability
                    : CalculationMemoryStoragePrototypeBenchmark.Reachability.values()) {
                var state = new CalculationMemoryStoragePrototypeBenchmark.BindingState();
                state.configure(slotCount, reachability);
                state.setUp();
                long columnarBytes = GraphLayout.parseInstance(state.columnarMemory()).totalSize();
                long appendColumnarBytes = GraphLayout.parseInstance(state.appendColumnarMemory()).totalSize();
                long eagerBytes = GraphLayout.parseInstance(state.eagerMemory()).totalSize();
                System.out.printf(
                        "%d,%s,%d,%d,%d%n",
                        slotCount,
                        reachability,
                        columnarBytes,
                        appendColumnarBytes,
                        eagerBytes);
            }
        }

        System.out.println("shape,frameColumnarBytes,appendColumnarBytes");
        for (CalculationMemoryStoragePrototypeBenchmark.RepresentativeShape shape
                : CalculationMemoryStoragePrototypeBenchmark.RepresentativeShape.values()) {
            var state = new CalculationMemoryStoragePrototypeBenchmark.RepresentativeState();
            state.configure(shape);
            state.setUp();
            System.out.printf(
                    "%s,%d,%d%n",
                    shape,
                    GraphLayout.parseInstance(state.columnarMemory()).totalSize(),
                    GraphLayout.parseInstance(state.appendColumnarMemory()).totalSize());
        }
    }
}
