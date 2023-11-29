package com.runestone.dynafilter.core.operation;

import com.runestone.dynafilter.core.exceptions.FilterOperationNotDefinedException;
import com.runestone.dynafilter.core.model.FilterData;

import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public abstract class AbstractFilterOperationService<T> implements FilterOperationService<T> {

    private final Map<Class<? super DefinedFilterOperation>, FilterOperation<T>> operationMap;

    public AbstractFilterOperationService(Supplier<Map<Class<? super DefinedFilterOperation>, FilterOperation<T>>> operationMap) {
        this.operationMap = operationMap.get();
    }

    @Override
    public T createFilter(FilterData filterData) {
        Objects.requireNonNull(filterData, "filterData cannot be null");
        FilterOperation<T> filterOperation = operationMap.get(filterData.operation());
        if (filterOperation == null) {
            throw new FilterOperationNotDefinedException(String.format("No filter found for operation '%s' on service %s",
                    filterData.operation().getSimpleName(), this.getClass().getCanonicalName()));
        }
        return filterOperation.createFilter(filterData);
    }

}
