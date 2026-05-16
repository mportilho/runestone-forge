package com.runestone.dynafilter.core.generator;

import com.runestone.dynafilter.core.exception.StatementGenerationException;
import com.runestone.dynafilter.core.model.FilterData;
import com.runestone.dynafilter.core.operation.Equals;
import com.runestone.dynafilter.core.operation.GreaterOrEquals;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DefaultStatementGeneratorTest {

    private final DefaultStatementGenerator generator = new DefaultStatementGenerator(value -> "resolved:" + value);

    @Test
    @DisplayName("constant values have precedence over request parameters and defaults")
    void constantValuesHavePrecedence() {
        Object[] values = generator.computeValues(
                new String[]{"name"},
                new Object[]{"default"},
                new Object[]{"constant"},
                Map.of("name", "request")
        );

        assertThat(values).containsExactly("resolved:constant");
    }

    @Test
    @DisplayName("request parameters have precedence over defaults")
    void requestParametersHavePrecedenceOverDefaults() {
        Object[] values = generator.computeValues(
                new String[]{"name"},
                new Object[]{"default"},
                new Object[0],
                Map.of("name", "request")
        );

        assertThat(values).containsExactly("request");
    }

    @Test
    @DisplayName("defaults are resolved when request parameter is absent")
    void defaultsAreResolvedWhenRequestIsAbsent() {
        Object[] values = generator.computeValues(
                new String[]{"name"},
                new Object[]{"default"},
                new Object[0],
                Map.of()
        );

        assertThat(values).containsExactly("resolved:default");
    }

    @Test
    @DisplayName("blank parameter names fail before value computation")
    void blankParameterNamesFail() {
        assertThatThrownBy(() -> generator.computeValues(new String[]{" "}, new Object[0], new Object[0], Map.of()))
                .isInstanceOf(StatementGenerationException.class)
                .hasMessageContaining("blank");
    }

    @Test
    @DisplayName("createFilterData resolves dynamic operation and wraps invalid formats")
    void createFilterDataResolvesDynamicOperation() {
        FilterData filterData = generator.createFilterData(
                "age",
                new String[]{"age"},
                Integer.class,
                com.runestone.dynafilter.core.operation.Dynamic.class,
                false,
                new Object[]{"NGE", 18},
                List.of(),
                "Age"
        );

        assertThat(filterData.operation()).isEqualTo(GreaterOrEquals.class);
        assertThat(filterData.negate()).isTrue();
        assertThat(filterData.values()).containsExactly(18);
    }

    @Test
    @DisplayName("createFilterData creates non dynamic filter data")
    void createFilterDataCreatesNonDynamicFilterData() {
        FilterData filterData = generator.createFilterData(
                "name",
                new String[]{"name"},
                String.class,
                Equals.class,
                true,
                new Object[]{"Ada"},
                List.of(),
                "Name"
        );

        assertThat(filterData.operation()).isEqualTo(Equals.class);
        assertThat(filterData.negate()).isTrue();
    }
}
