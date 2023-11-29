package com.runestone.dynafilter.core.generator;

import com.runestone.dynafilter.core.model.FilterData;
import com.runestone.dynafilter.core.model.statement.AbstractStatement;

import java.util.Map;
import java.util.Optional;

/**
 * Wrapper class for a {@link AbstractStatement} and a {@link FilterData} map of decorated filters.
 *
 * @param statement        the statement for creating filters
 * @param decoratedFilters the map of decorated filters
 * @author Marcelo Portilho
 */
public record StatementWrapper(AbstractStatement statement, Map<String, FilterData> decoratedFilters) {

    public StatementWrapper(AbstractStatement statement, Map<String, FilterData> decoratedFilters) {
        if (statement == null) {
            throw new IllegalArgumentException("Statement cannot be null");
        }
        this.statement = statement;
        this.decoratedFilters = decoratedFilters != null ? decoratedFilters : Map.of();
    }

    public Optional<FilterData> findDecoratedFilterByPath(String path) {
        return Optional.ofNullable(decoratedFilters.get(path));
    }
}
