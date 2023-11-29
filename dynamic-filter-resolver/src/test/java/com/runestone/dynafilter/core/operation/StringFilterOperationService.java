package com.runestone.dynafilter.core.operation;

import com.runestone.dynafilter.core.operation.types.Equals;

import java.util.Map;

public class StringFilterOperationService extends AbstractFilterOperationService<String> {

    public StringFilterOperationService() {
        super(() -> Map.of(
                Equals.class, new EqualsTestFilter()
        ));
    }
}
