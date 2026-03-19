package com.runestone.expeval2.internal.runtime;

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
    @DisplayName("coerces numeric vector to Comparable array")
    void coercesNumericVectorToComparableArray() {
        RuntimeValue.VectorValue vector = new RuntimeValue.VectorValue(List.of(
                new RuntimeValue.NumberValue(new BigDecimal("10")),
                new RuntimeValue.NumberValue(new BigDecimal("20"))
        ));

        Object result = coercionService.coerce(vector, Comparable[].class);

        assertThat(result).isInstanceOf(Comparable[].class);
        assertThat((Comparable<?>[]) result).containsExactly(
                new BigDecimal("10"),
                new BigDecimal("20")
        );
    }

    @Test
    @DisplayName("coerces string vector to String array")
    void coercesStringVectorToStringArray() {
        RuntimeValue.VectorValue vector = new RuntimeValue.VectorValue(List.of(
                new RuntimeValue.StringValue("alpha"),
                new RuntimeValue.StringValue("beta")
        ));

        Object result = coercionService.coerce(vector, String[].class);

        assertThat(result).isInstanceOf(String[].class);
        assertThat((String[]) result).containsExactly("alpha", "beta");
    }
}
