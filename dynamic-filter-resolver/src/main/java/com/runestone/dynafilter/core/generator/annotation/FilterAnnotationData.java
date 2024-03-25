package com.runestone.dynafilter.core.generator.annotation;

import com.runestone.dynafilter.core.model.statement.LogicOperator;

import java.util.List;

public record FilterAnnotationData(
        LogicOperator logicOperator, List<Filter> filters, List<FilterAnnotationStatement> filterStatements, String negate) {
}
