package com.runestone.expeval_mk3.perf.jmh;

import com.runestone.expeval_mk3.internal.runtime.ExecutionScope;
import com.runestone.expeval_mk3.internal.runtime.SafeExecutionScope;
import com.runestone.expeval_mk3.internal.runtime.TraversalLimitedExecutionScope;
import org.junit.jupiter.api.Test;
import org.openjdk.jol.info.ClassLayout;

import static org.assertj.core.api.Assertions.assertThat;

class TraversalScopeLayoutProbeTest {

    @Test
    void traversalCounterDoesNotGrowScalarScopeLayouts() {
        assertThat(ExecutionScope.class.getDeclaredFields()).extracting(java.lang.reflect.Field::getName)
                .doesNotContain("remaining", "maximum");
        assertThat(SafeExecutionScope.class.getDeclaredFields()).extracting(java.lang.reflect.Field::getName)
                .doesNotContain("remaining", "maximum");
        assertThat(TraversalLimitedExecutionScope.class.getDeclaredFields())
                .extracting(java.lang.reflect.Field::getName).containsExactlyInAnyOrder("remaining", "maximum");
        assertThat(ClassLayout.parseClass(TraversalLimitedExecutionScope.class).instanceSize())
                .isGreaterThanOrEqualTo(ClassLayout.parseClass(SafeExecutionScope.class).instanceSize());
    }
}
