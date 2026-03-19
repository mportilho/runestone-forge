package com.runestone.expeval2.internal.runtime;

import com.runestone.converters.impl.DefaultDataConversionService;
import com.runestone.expeval2.types.ScalarType;
import com.runestone.expeval2.types.VectorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RuntimeValueFactory")
class RuntimeValueFactoryTest {

    private final RuntimeValueFactory runtimeValueFactory =
            new RuntimeValueFactory(new DefaultDataConversionService());

    @Test
    @DisplayName("wraps exact BigDecimal scalar without altering numeric value")
    void wrapsExactBigDecimalScalar() {
        BigDecimal rawValue = new BigDecimal("42.75");

        RuntimeValue result = runtimeValueFactory.from(rawValue, ScalarType.NUMBER);

        assertThat(result).isInstanceOf(RuntimeValue.NumberValue.class);
        assertThat(((RuntimeValue.NumberValue) result).value()).isSameAs(rawValue);
    }

    @Test
    @DisplayName("wraps object arrays as numeric vectors")
    void wrapsObjectArraysAsNumericVectors() {
        BigDecimal[] rawValue = {new BigDecimal("10"), new BigDecimal("20")};

        RuntimeValue result = runtimeValueFactory.from(rawValue, VectorType.INSTANCE);

        assertThat(result).isInstanceOf(RuntimeValue.VectorValue.class);
        assertThat(((RuntimeValue.VectorValue) result).elements()).containsExactly(
                new RuntimeValue.NumberValue(new BigDecimal("10")),
                new RuntimeValue.NumberValue(new BigDecimal("20"))
        );
    }

    @Test
    @DisplayName("wraps iterables as numeric vectors")
    void wrapsIterablesAsNumericVectors() {
        RuntimeValue result = runtimeValueFactory.from(List.of(1, 2, 3), VectorType.INSTANCE);

        assertThat(result).isInstanceOf(RuntimeValue.VectorValue.class);
        assertThat(((RuntimeValue.VectorValue) result).elements()).containsExactly(
                new RuntimeValue.NumberValue(BigDecimal.ONE),
                new RuntimeValue.NumberValue(BigDecimal.valueOf(2L)),
                new RuntimeValue.NumberValue(BigDecimal.valueOf(3L))
        );
    }

    @Test
    @DisplayName("wraps exact temporal scalar without conversion")
    void wrapsExactTemporalScalar() {
        LocalDate rawValue = LocalDate.of(2026, 3, 19);

        RuntimeValue result = runtimeValueFactory.from(rawValue, ScalarType.DATE);

        assertThat(result).isInstanceOf(RuntimeValue.DateValue.class);
        assertThat(((RuntimeValue.DateValue) result).value()).isSameAs(rawValue);
    }
}
