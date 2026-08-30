package com.runestone.expeval_mk3.internal.runtime;

/** Executable source boundary that may publish one calculation-memory value. */
interface CalculationPointExecutableNode extends ExecutableNode {

    int calculationSlot();

    int[] replaySlots();
}
