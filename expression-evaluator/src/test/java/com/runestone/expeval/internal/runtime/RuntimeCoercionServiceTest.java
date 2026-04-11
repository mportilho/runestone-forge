package com.runestone.expeval.internal.runtime;

import com.runestone.converters.impl.DefaultDataConversionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RuntimeCoercionService")
class RuntimeCoercionServiceTest {

    private final RuntimeCoercionService coercionService =
            new RuntimeCoercionService(new DefaultDataConversionService());

    @Test
    @DisplayName("coerces numeric list to Comparable array")
    void coercesNumericListToComparableArray() {
        List<Object> vector = List.of(new BigDecimal("10"), new BigDecimal("20"));

        Object result = coercionService.coerce(vector, Comparable[].class);

        assertThat(result).isInstanceOf(Comparable[].class);
        assertThat((Comparable<?>[]) result).containsExactly(
                new BigDecimal("10"),
                new BigDecimal("20")
        );
    }

    @Test
    @DisplayName("coerces string list to String array")
    void coercesStringListToStringArray() {
        List<Object> vector = List.of("alpha", "beta");

        Object result = coercionService.coerce(vector, String[].class);

        assertThat(result).isInstanceOf(String[].class);
        assertThat((String[]) result).containsExactly("alpha", "beta");
    }

    @Test
    @DisplayName("coerces numeric scalar to primitive and boxed number targets")
    void coercesNumericScalarToPrimitiveAndBoxedNumberTargets() {
        BigDecimal value = new BigDecimal("42");

        assertThat(coercionService.coerce(value, double.class)).isEqualTo(42.0d);
        assertThat(coercionService.coerce(value, Double.class)).isEqualTo(42.0d);
        assertThat(coercionService.coerce(value, int.class)).isEqualTo(42);
        assertThat(coercionService.coerce(value, Integer.class)).isEqualTo(42);
        assertThat(coercionService.coerce(value, long.class)).isEqualTo(42L);
        assertThat(coercionService.coerce(value, Long.class)).isEqualTo(42L);
    }
}
