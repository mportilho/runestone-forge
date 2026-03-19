package com.runestone.expeval2.internal.runtime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RuntimeValue")
class RuntimeValueTest {

    @Test
    @DisplayName("vector value keeps trusted elements list by reference")
    void vectorValueKeepsTrustedElementsListByReference() {
        List<RuntimeValue> source = new ArrayList<>();
        source.add(new RuntimeValue.NumberValue(BigDecimal.ONE));

        RuntimeValue.VectorValue value = new RuntimeValue.VectorValue(source);
        source.add(new RuntimeValue.NumberValue(BigDecimal.TEN));

        assertThat(value.elements()).isSameAs(source);
        assertThat(value.elements()).containsExactly(
                new RuntimeValue.NumberValue(BigDecimal.ONE),
                new RuntimeValue.NumberValue(BigDecimal.TEN)
        );
    }

    @Test
    @DisplayName("vector value preserves value-based equality")
    void vectorValuePreservesValueBasedEquality() {
        RuntimeValue.VectorValue left = new RuntimeValue.VectorValue(new ArrayList<>(List.of(
                new RuntimeValue.NumberValue(BigDecimal.ONE),
                new RuntimeValue.NumberValue(BigDecimal.TEN)
        )));
        RuntimeValue.VectorValue right = new RuntimeValue.VectorValue(new ArrayList<>(List.of(
                new RuntimeValue.NumberValue(BigDecimal.ONE),
                new RuntimeValue.NumberValue(BigDecimal.TEN)
        )));

        assertThat(left).isEqualTo(right);
        assertThat(left.hashCode()).isEqualTo(right.hashCode());
    }
}
