package com.runestone.dynafilter.core.generator;

import com.runestone.dynafilter.core.model.FilterData;
import com.runestone.dynafilter.core.operation.Equals;
import com.runestone.dynafilter.core.statement.LogicalStatement;
import com.runestone.dynafilter.core.statement.NoOpStatement;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StatementWrapperTest {

    @Test
    @DisplayName("normalizes optional collections to empty immutable collections")
    void normalizesOptionalCollections() {
        StatementWrapper wrapper = new StatementWrapper(new NoOpStatement(), null, null);

        assertThat(wrapper.decoratedFilters()).isEmpty();
        assertThat(wrapper.allFilters()).isEmpty();
        assertThatThrownBy(() -> wrapper.decoratedFilters().put("name", filterData()))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> wrapper.allFilters().add(null))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("keeps decorated filters keyed by filter path")
    void keepsDecoratedFiltersByPath() {
        FilterData filterData = filterData();

        StatementWrapper wrapper = new StatementWrapper(
                new LogicalStatement(filterData),
                Map.of(filterData.path(), filterData),
                List.of()
        );

        assertThat(wrapper.decoratedFilters()).containsEntry("name", filterData);
    }

    private static FilterData filterData() {
        return new FilterData(
                "name",
                new String[]{"name"},
                String.class,
                Equals.class,
                false,
                new Object[]{"Ada"},
                List.of(),
                ""
        );
    }
}
