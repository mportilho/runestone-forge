package com.runestone.dynafilter.core.generator;

import com.runestone.dynafilter.core.model.FilterData;
import com.runestone.dynafilter.core.model.FilterRequestData;
import com.runestone.dynafilter.core.statement.AbstractStatement;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public record StatementWrapper(
        AbstractStatement statement,
        Map<String, FilterData> decoratedFilters,
        List<FilterRequestData> allFilters
) {

    public StatementWrapper {
        statement = Objects.requireNonNull(statement, "statement must not be null");
        decoratedFilters = decoratedFilters == null ? Map.of() : Map.copyOf(decoratedFilters);
        allFilters = allFilters == null ? List.of() : List.copyOf(allFilters);
    }
}
