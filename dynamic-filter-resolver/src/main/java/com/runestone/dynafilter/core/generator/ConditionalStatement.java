package com.runestone.dynafilter.core.generator;

import com.runestone.dynafilter.core.resolver.FilterDecorator;

public record ConditionalStatement(StatementWrapper statementWrapper, FilterDecorator<?> filterDecorator) {

    public ConditionalStatement {
        if (statementWrapper == null) {
            throw new IllegalArgumentException("Statement Wrapper cannot be null");
        }
    }
}
