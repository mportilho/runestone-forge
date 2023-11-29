package com.runestone.dynafilter.core.generator;

import com.runestone.dynafilter.core.model.statement.AbstractStatement;

import java.util.Map;

/**
 * Interface for classes that generate {@link AbstractStatement} instances that will be used to build a filter.
 *
 * <p>
 * Obs.1: The generator is responsible for interpreting the {@link com.runestone.dynafilter.core.operation.types.Dynamic} operation type
 * into the corresponding operation type (Equals, Like, etc).
 * </p>
 *
 * @param <T> the type of the filter inputs
 */
public interface StatementGenerator<T> {

    /**
     * Creates a new instance of {@link AbstractStatement} that will be used to build a filter.
     *
     * @param filterInputs     inputs containing enough information to build the filter
     * @param filterParameters map of parameters providing values to be used to build the filter
     * @return a new instance of {@link AbstractStatement}
     */
    StatementWrapper generateStatements(T filterInputs, Map<String, Object> filterParameters);

}
