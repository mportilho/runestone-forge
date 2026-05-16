package com.runestone.dynafilter.core.statement;

import com.runestone.dynafilter.core.model.FilterData;

import java.util.Objects;

public final class LogicalStatement extends AbstractStatement {

    private final FilterData filterData;

    public LogicalStatement(FilterData filterData) {
        this.filterData = Objects.requireNonNull(filterData, "filterData must not be null");
    }

    public FilterData filterData() {
        return filterData;
    }
}
