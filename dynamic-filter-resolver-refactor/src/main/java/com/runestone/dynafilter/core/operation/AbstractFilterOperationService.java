package com.runestone.dynafilter.core.operation;

import com.runestone.dynafilter.core.exception.FilterOperationNotDefinedException;
import com.runestone.dynafilter.core.model.FilterData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class AbstractFilterOperationService<T> implements FilterOperationService<T> {

    private final Map<Class<? extends DefinedFilterOperation>, FilterOperation<T>> operations;

    protected AbstractFilterOperationService(List<? extends FilterOperation<T>> operations) {
        Map<Class<? extends DefinedFilterOperation>, FilterOperation<T>> indexedOperations = new HashMap<>();
        for (FilterOperation<T> operation : operations == null ? List.<FilterOperation<T>>of() : operations) {
            indexedOperations.put(operation.operationType(), operation);
        }
        this.operations = Map.copyOf(indexedOperations);
    }

    @Override
    public T createFilter(FilterData filterData) {
        Objects.requireNonNull(filterData, "filterData must not be null");
        FilterOperation<T> operation = operations.get(filterData.operation());
        if (operation == null) {
            throw new FilterOperationNotDefinedException("Filter operation not defined: " + filterData.operation());
        }
        return operation.createFilter(filterData);
    }
}
