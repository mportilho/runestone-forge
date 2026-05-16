package com.runestone.dynafilter.core.operation;

import com.runestone.dynafilter.core.model.FilterData;

public interface FilterOperation<T> {

    Class<? extends DefinedFilterOperation> operationType();

    T createFilter(FilterData filterData);
}
