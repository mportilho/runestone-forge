package com.runestone.dynafilter.core.operation;

import com.runestone.dynafilter.core.model.FilterData;

public class EqualsTestFilter implements FilterOperation<String> {

    @Override
    public String createFilter(FilterData filterData) {
        return "=" + filterData.findOneValue();
    }
}
