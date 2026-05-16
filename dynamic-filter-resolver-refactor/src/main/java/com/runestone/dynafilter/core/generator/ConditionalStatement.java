package com.runestone.dynafilter.core.generator;

import com.runestone.dynafilter.core.decorator.FilterDecorator;

import java.util.Objects;

public record ConditionalStatement(
        StatementWrapper statementWrapper,
        FilterDecorator<?> filterDecorator
) {

    public ConditionalStatement {
        statementWrapper = Objects.requireNonNull(statementWrapper, "statementWrapper must not be null");
    }
}
