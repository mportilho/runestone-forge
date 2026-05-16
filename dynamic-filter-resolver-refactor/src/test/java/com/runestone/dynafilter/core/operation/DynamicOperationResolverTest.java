package com.runestone.dynafilter.core.operation;

import com.runestone.dynafilter.core.exception.DynamicFilterConfigurationException;
import com.runestone.dynafilter.core.model.FilterData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DynamicOperationResolverTest {

    private final DynamicOperationResolver resolver = new DynamicOperationResolver();

    @Test
    @DisplayName("resolves positive two-character operation code")
    void resolvesPositiveOperationCode() {
        FilterData filterData = resolver.resolve(request(new Object[]{"EQ", "Ada"}));

        assertThat(filterData.operation()).isEqualTo(Equals.class);
        assertThat(filterData.negate()).isFalse();
        assertThat(filterData.parameters()).containsExactly("name");
        assertThat(filterData.values()).containsExactly("Ada");
    }

    @Test
    @DisplayName("resolves negated three-character operation code")
    void resolvesNegatedOperationCode() {
        FilterData filterData = resolver.resolve(request(new Object[]{"nGE", 10}));

        assertThat(filterData.operation()).isEqualTo(GreaterOrEquals.class);
        assertThat(filterData.negate()).isTrue();
        assertThat(filterData.values()).containsExactly(10);
    }

    @Test
    @DisplayName("groups multiple IN values as one array value")
    void groupsMultipleInValuesAsArray() {
        FilterData filterData = resolver.resolve(request(new Object[]{"IN", "Ada", "Grace"}));

        assertThat(filterData.operation()).isEqualTo(IsIn.class);
        assertThat(filterData.parameters()).containsExactly("name");
        assertThat(filterData.values())
                .singleElement()
                .isInstanceOf(Object[].class)
                .satisfies(value -> assertThat((Object[]) value).containsExactly("Ada", "Grace"));
    }

    @Test
    @DisplayName("renames BT parameters to From and To")
    void renamesBetweenParameters() {
        FilterData filterData = resolver.resolve(request(new Object[]{"BT", 10, 20}));

        assertThat(filterData.operation()).isEqualTo(Between.class);
        assertThat(filterData.parameters()).containsExactly("nameFrom", "nameTo");
        assertThat(filterData.values()).containsExactly(10, 20);
    }

    @Test
    @DisplayName("rejects dynamic value that is not an Object array")
    void rejectsNonArrayValue() {
        assertThatThrownBy(() -> resolver.resolve(request("EQ")))
                .isInstanceOf(DynamicFilterConfigurationException.class)
                .hasMessageContaining("Object[]");
    }

    @Test
    @DisplayName("rejects invalid operation code")
    void rejectsInvalidOperationCode() {
        assertThatThrownBy(() -> resolver.resolve(request(new Object[]{"ZZ", "Ada"})))
                .isInstanceOf(DynamicFilterConfigurationException.class)
                .hasMessageContaining("Unknown dynamic operation code");
    }

    @Test
    @DisplayName("rejects dynamic value without operation code")
    void rejectsDynamicValueWithoutOperationCode() {
        assertThatThrownBy(() -> resolver.resolve(request(new Object[0])))
                .isInstanceOf(DynamicFilterConfigurationException.class)
                .hasMessageContaining("operation code");
    }

    @Test
    @DisplayName("rejects dynamic value whose first item is not an operation code")
    void rejectsDynamicValueWhoseFirstItemIsNotOperationCode() {
        assertThatThrownBy(() -> resolver.resolve(request(new Object[]{1, "Ada"})))
                .isInstanceOf(DynamicFilterConfigurationException.class)
                .hasMessageContaining("operation code");
    }

    @Test
    @DisplayName("rejects BT with different value count than two")
    void rejectsBetweenWithInvalidValueCount() {
        assertThatThrownBy(() -> resolver.resolve(request(new Object[]{"BT", 10})))
                .isInstanceOf(DynamicFilterConfigurationException.class)
                .hasMessageContaining("exactly two values");
    }

    private static DynamicOperationRequest request(Object value) {
        return new DynamicOperationRequest("name", "name", String.class, value, List.of(), "Person name");
    }
}
