package com.runestone.dynafilter.core.operation;

import com.runestone.dynafilter.core.model.FilterData;

public interface FilterOperationService<T> {

    T createFilter(FilterData filterData);

}
