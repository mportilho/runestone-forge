package com.runestone.dynafilter.core.operation;

import com.runestone.dynafilter.core.model.FilterData;

/**
 * Interface that defines the contract for creating objects that will act on search filters
 *
 * @param <R> the type of the object responsible for filtering
 */
public interface FilterOperation<R> {

    /**
     * Creates the object responsible for filtering
     *
     * @param filterData the filter data to be used to create the filter
     * @return the object responsible for filtering
     */
    R createFilter(FilterData filterData);

}
