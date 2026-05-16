package com.runestone.dynafilter.core.generator.annotation;

import com.runestone.dynafilter.core.annotation.Filter;
import com.runestone.dynafilter.core.statement.LogicOperator;

import java.util.List;
import java.util.Objects;

public record FilterAnnotationData(
        LogicOperator logicOperator,
        List<Filter> filters,
        List<FilterAnnotationStatement> filterStatements,
        String negate
) {

    public FilterAnnotationData {
        logicOperator = Objects.requireNonNull(logicOperator, "logicOperator must not be null");
        filters = filters == null ? List.of() : List.copyOf(filters);
        filterStatements = filterStatements == null ? List.of() : List.copyOf(filterStatements);
    }
}
