package com.runestone.expeval.internal.runtime;

/** Executable source boundary that may publish one calculation-memory value. */
interface CalculationPointExecutableNode extends ExecutableNode {

    int calculationSlot();

    int[] replaySlots();
}
