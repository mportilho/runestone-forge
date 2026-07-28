package com.runestone.expeval_mk3.internal.plan;

import com.runestone.expeval_mk3.internal.source.SourceSpan;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExecutionPlanBuilderTest {

    private static final SourceSpan SPAN = new SourceSpan(2, 13, 1, 3);

    @Test
    void mapStringKeySubscriptRejectsNullValuesAtRuntime() {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("present", null);

        assertThatThrownBy(() -> ExecutionPlanBuilder.mapKeyValue(values, "present", false, SPAN))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("map value at SourceSpan[offset=2, endOffset=13, line=1, column=3]");
    }

    @Test
    void safeMapStringKeySubscriptOnlyProtectsNullReceivers() {
        assertThat(ExecutionPlanBuilder.mapKeyValue(null, "present", true, SPAN)).isNull();

        assertThatThrownBy(() -> ExecutionPlanBuilder.mapKeyValue(
                Map.of("present", BigDecimal.ONE), "missing", true, SPAN))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("map key not found: missing");
    }
}
