package com.runestone.dynafilter.core;

import com.runestone.dynafilter.core.generator.ConditionalStatement;
import com.runestone.dynafilter.core.decorator.FilterDecorator;
import com.runestone.dynafilter.core.generator.StatementWrapper;

public interface DynamicFilterResolver<T> {

    T createFilter(StatementWrapper statementWrapper, FilterDecorator<T> decorator);

    default T createFilter(ConditionalStatement conditionalStatement) {
        return createFilter(conditionalStatement.statementWrapper(), null);
    }
}
