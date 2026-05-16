package com.runestone.dynafilter.core.model;

import com.runestone.dynafilter.core.annotation.Filter;
import com.runestone.dynafilter.core.modifier.ModIgnoreCase;
import com.runestone.dynafilter.core.operation.Equals;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FilterRequestDataTest {

    @Test
    @DisplayName("copies metadata from filter annotation")
    void copiesMetadataFromFilterAnnotation() throws NoSuchFieldException {
        Field field = AnnotatedFilters.class.getDeclaredField("name");
        Filter filter = field.getAnnotation(Filter.class);

        FilterRequestData requestData = FilterRequestData.from(filter);

        assertThat(requestData.path()).isEqualTo("name");
        assertThat(requestData.parameters()).containsExactly("name");
        assertThat(requestData.targetType()).isEqualTo(String.class);
        assertThat(requestData.operation()).isEqualTo(Equals.class);
        assertThat(requestData.negate()).isEqualTo("false");
        assertThat(requestData.defaultValues()).containsExactly("unknown");
        assertThat(requestData.modifiers()).containsExactly(ModIgnoreCase.class);
        assertThat(requestData.description()).isEqualTo("Person name");
    }

    @Test
    @DisplayName("constant values have explicit presence semantics")
    void reportsConstantValuePresence() {
        FilterRequestData requestData = new FilterRequestData(
                "status",
                new String[]{"status"},
                String.class,
                Equals.class,
                "false",
                new Object[0],
                new Object[]{"ACTIVE"},
                "",
                false,
                List.of(),
                ""
        );

        assertThat(requestData.hasConstantValues()).isTrue();
        assertThat(requestData.hasDefaultValues()).isFalse();
    }

    @Test
    @DisplayName("rejects default values with size different from parameters")
    void rejectsMismatchedDefaultValues() {
        assertThatThrownBy(() -> new FilterRequestData(
                "name",
                new String[]{"first", "second"},
                String.class,
                Equals.class,
                "false",
                new Object[]{"Ada"},
                new Object[0],
                "",
                false,
                List.of(),
                ""
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("defaultValues");
    }

    private static class AnnotatedFilters {

        @Filter(
                path = "name",
                parameters = "name",
                targetType = String.class,
                operation = Equals.class,
                defaultValues = "unknown",
                modifiers = ModIgnoreCase.class,
                description = "Person name"
        )
        private String name;
    }
}
