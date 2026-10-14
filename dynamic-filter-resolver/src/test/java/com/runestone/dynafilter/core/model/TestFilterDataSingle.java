package com.runestone.dynafilter.core.model;

import com.runestone.dynafilter.core.operation.types.Equals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

final class TestFilterDataSingle {

    @Test
    void createsOneSlotWithExplicitParameterMetadataAndNormalDefaults() {
        var filter = FilterData.single("name", "clientName", String.class, Equals.class, "John");

        assertThat(filter.path()).containsExactly("name");
        assertThat(filter.parameters()).containsExactly("clientName");
        assertThat(filter.targetType()).isEqualTo(String.class);
        assertThat(filter.operation()).isEqualTo(Equals.class);
        assertThat(filter.values()).containsExactly("John");
        assertThat(filter.<String>findOneValue()).isEqualTo("John");
        assertThat(filter.negate()).isFalse();
        assertThat(filter.modifiers()).isNull();
        assertThat(filter.description()).isNull();
    }

    @Test
    void defaultsParameterMetadataToThePathWithoutChangingDefaults() {
        var filter = FilterData.single("name", String.class, Equals.class, "John");

        assertThat(filter.path()).containsExactly("name");
        assertThat(filter.parameters()).containsExactly("name");
        assertThat(filter.targetType()).isEqualTo(String.class);
        assertThat(filter.operation()).isEqualTo(Equals.class);
        assertThat(filter.values()).containsExactly("John");
        assertThat(filter.negate()).isFalse();
        assertThat(filter.modifiers()).isNull();
        assertThat(filter.description()).isNull();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t\n"})
    void rejectsInvalidPathsInBothForms(String path) {
        assertThatThrownBy(() -> FilterData.single(path, "clientName", String.class, Equals.class, "John"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> FilterData.single(path, String.class, Equals.class, "John"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t\n"})
    void rejectsInvalidExplicitParameterNames(String parameter) {
        assertThatThrownBy(() -> FilterData.single("name", parameter, String.class, Equals.class, "John"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void keepsTheExplicitTargetTypeWithoutConvertingTheInputInEitherForm() {
        var explicit = FilterData.single("id", "selectedId", Long.class, Equals.class, "42");
        var abbreviated = FilterData.single("id", Long.class, Equals.class, "42");

        assertThat(explicit.targetType()).isEqualTo(Long.class);
        assertThat(explicit.values()).containsExactly("42");
        assertThat(abbreviated.targetType()).isEqualTo(Long.class);
        assertThat(abbreviated.values()).containsExactly("42");
    }

    @Test
    void retainsNullAsOneValueSlotInEitherForm() {
        var explicit = FilterData.single("name", "clientName", String.class, Equals.class, null);
        var abbreviated = FilterData.single("name", String.class, Equals.class, null);

        assertThat(explicit.values()).hasSize(1);
        assertThat(explicit.values()[0]).isNull();
        assertThat(explicit.<Object>findOneValue()).isNull();
        assertThat(abbreviated.values()).hasSize(1);
        assertThat(abbreviated.values()[0]).isNull();
        assertThat(abbreviated.<Object>findOneValue()).isNull();
    }

    @Test
    void retainsAReferenceArrayAsOneSharedValueInEitherForm() {
        String[] names = {"John", "Mary"};
        var explicit = FilterData.single("name", "clientName", String.class, Equals.class, names);
        var abbreviated = FilterData.single("name", String.class, Equals.class, names);

        assertThat(explicit.values()).hasSize(1);
        assertThat(explicit.values()[0]).isSameAs(names);
        assertThat(explicit.<Object>findOneValue()).isSameAs(names);
        assertThat(abbreviated.values()).hasSize(1);
        assertThat(abbreviated.values()[0]).isSameAs(names);
        assertThat(abbreviated.<Object>findOneValue()).isSameAs(names);
    }

    @Test
    void retainsAPrimitiveArrayAsOneSharedValueInEitherForm() {
        int[] ids = {1, 2};
        var explicit = FilterData.single("id", "selectedIds", Integer.class, Equals.class, ids);
        var abbreviated = FilterData.single("id", Integer.class, Equals.class, ids);

        assertThat(explicit.values()).hasSize(1);
        assertThat(explicit.values()[0]).isSameAs(ids);
        assertThat(explicit.<Object>findOneValue()).isSameAs(ids);
        assertThat(abbreviated.values()).hasSize(1);
        assertThat(abbreviated.values()[0]).isSameAs(ids);
        assertThat(abbreviated.<Object>findOneValue()).isSameAs(ids);
    }
}
