package com.runestone.dynafilter.core.generator;

import com.runestone.dynafilter.core.model.FilterData;
import com.runestone.dynafilter.core.model.statement.AbstractStatement;
import com.runestone.dynafilter.core.resolver.FilterDecorator;

import java.util.Map;

public record ConditionalStatement(AbstractStatement statement, Map<String, FilterData> decoratedFilterDataMap,
                                   FilterDecorator<?> filterDecorator) {

    public ConditionalStatement(AbstractStatement statement, Map<String, FilterData> decoratedFilterDataMap, FilterDecorator<?> filterDecorator) {
        if (statement == null) {
            throw new IllegalArgumentException("Statement cannot be null");
        }
        this.statement = statement;
        this.decoratedFilterDataMap = decoratedFilterDataMap != null ? decoratedFilterDataMap : Map.of();
        this.filterDecorator = filterDecorator;
    }
}
