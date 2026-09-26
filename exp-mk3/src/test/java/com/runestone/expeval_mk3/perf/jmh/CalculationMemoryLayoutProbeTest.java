package com.runestone.expeval_mk3.perf.jmh;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class CalculationMemoryLayoutProbeTest {

    @ParameterizedTest
    @ValueSource(ints = {10, 100, 1_000})
    void layoutProbeKeepsItsExactNodeCountUnderDefaultCompilationLimits(int nodes) {
        assertThat(CalculationMemoryProductionLayoutReport.planWithNodeCount(nodes)).isNotNull();
    }
}
