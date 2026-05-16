package com.runestone.dynafilter.core.model;

import com.runestone.dynafilter.core.modifier.ModIgnoreCase;
import com.runestone.dynafilter.core.operation.Equals;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FilterDataTest {

    @Test
    @DisplayName("creates defensive copies for array and modifier inputs")
    void createsDefensiveCopiesForInputs() {
        String[] parameters = {"name"};
        Object[] values = {"Ada"};
        FilterData filterData = new FilterData(
                "name",
                parameters,
                String.class,
                Equals.class,
                false,
                values,
                List.of(ModIgnoreCase.class),
                "Person name"
        );

        parameters[0] = "changed";
        values[0] = "changed";

        assertThat(filterData.parameters()).containsExactly("name");
        assertThat(filterData.values()).containsExactly("Ada");
        assertThat(filterData.modifiers()).containsExactly(ModIgnoreCase.class);
        assertThat(filterData.hasModifier(ModIgnoreCase.class)).isTrue();
    }

    @Test
    @DisplayName("rejects empty parameters")
    void rejectsEmptyParameters() {
        assertThatThrownBy(() -> new FilterData(
                "name",
                new String[0],
                String.class,
                Equals.class,
                false,
                new Object[]{"Ada"},
                List.of(),
                ""
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("parameters");
    }

    @Test
    @DisplayName("rejects values with a different size than parameters")
    void rejectsMismatchedValueCount() {
        assertThatThrownBy(() -> new FilterData(
                "name",
                new String[]{"first", "second"},
                String.class,
                Equals.class,
                false,
                new Object[]{"Ada"},
                List.of(),
                ""
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("same size");
    }
}
