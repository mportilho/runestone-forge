package com.runestone.dynafilter.core.generator.annotation;

import com.runestone.dynafilter.core.annotation.Filter;

import java.util.List;

public record FilterAnnotationStatement(
        List<Filter> filters,
        String negate
) {

    public FilterAnnotationStatement {
        filters = filters == null ? List.of() : List.copyOf(filters);
    }
}
