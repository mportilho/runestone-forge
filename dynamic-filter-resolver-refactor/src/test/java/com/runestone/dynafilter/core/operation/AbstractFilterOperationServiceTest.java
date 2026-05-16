package com.runestone.dynafilter.core.operation;

import com.runestone.dynafilter.core.exception.FilterOperationNotDefinedException;
import com.runestone.dynafilter.core.model.FilterData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AbstractFilterOperationServiceTest {

    @Test
    @DisplayName("dispatches filter data to registered operation")
    void dispatchesToRegisteredOperation() {
        TestOperationService service = new TestOperationService(List.of(new EqualsOperation()));

        String result = service.createFilter(filterData(Equals.class));

        assertThat(result).isEqualTo("name=Ada");
    }

    @Test
    @DisplayName("fails explicitly when operation is not registered")
    void failsWhenOperationIsNotRegistered() {
        TestOperationService service = new TestOperationService(List.of());

        assertThatThrownBy(() -> service.createFilter(filterData(Equals.class)))
                .isInstanceOf(FilterOperationNotDefinedException.class)
                .hasMessageContaining("Filter operation not defined");
    }

    private static FilterData filterData(Class<? extends DefinedFilterOperation> operation) {
        return new FilterData("name", new String[]{"name"}, String.class, operation, false, new Object[]{"Ada"}, List.of(), "");
    }

    private static class TestOperationService extends AbstractFilterOperationService<String> {
        private TestOperationService(List<? extends FilterOperation<String>> operations) {
            super(operations);
        }
    }

    private static class EqualsOperation implements FilterOperation<String> {

        @Override
        public Class<? extends DefinedFilterOperation> operationType() {
            return Equals.class;
        }

        @Override
        public String createFilter(FilterData filterData) {
            return filterData.path() + '=' + filterData.findOneValue();
        }
    }
}
