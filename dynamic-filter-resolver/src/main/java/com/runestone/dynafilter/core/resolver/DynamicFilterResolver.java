package com.runestone.dynafilter.core.resolver;

import com.runestone.dynafilter.core.generator.StatementWrapper;

public interface DynamicFilterResolver<T> {

    <R extends T> R createFilter(StatementWrapper statementWrapper, FilterDecorator<T> decorator);

}
