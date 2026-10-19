package com.runestone.dynafilter.core.generator;

import com.runestone.dynafilter.core.model.FilterData;
import com.runestone.dynafilter.core.model.FilterRequestData;
import com.runestone.dynafilter.core.model.statement.AbstractStatement;
import com.runestone.dynafilter.core.model.statement.CompoundStatement;
import com.runestone.dynafilter.core.model.statement.LogicOperator;
import com.runestone.dynafilter.core.model.statement.LogicalStatement;
import com.runestone.dynafilter.core.model.statement.NegatedStatement;
import com.runestone.dynafilter.core.model.statement.NoOpStatement;
import com.runestone.dynafilter.core.resolver.FilterDecorator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Mutable, reusable, non-thread-safe composition of a complete conditional statement.
 * Ordinary AND/OR operations associate from left to right; supplied expressions remain grouped operands.
 * Each build creates a new result, sharing existing trees and filter values under the non-mutation contract.
 */
public final class ConditionalStatementBuilder {

    private AbstractStatement ordinary = new NoOpStatement();
    private Map<String, FilterData> decoratedFilters = Map.of();
    private boolean ownsDecoratedFilters;
    private List<FilterRequestData> allFilters = List.of();
    private SortContract sortContract = SortContract.EMPTY;
    private final List<AbstractStatement> restrictions = new ArrayList<>();
    private final List<FilterDecorator<?>> decorators = new ArrayList<>();

    ConditionalStatementBuilder() {
    }

    ConditionalStatementBuilder(ConditionalStatement source) {
        Objects.requireNonNull(source, "Source statement cannot be null");
        ordinary = source.statementWrapper().statement();
        decoratedFilters = source.statementWrapper().decoratedFilters();
        allFilters = source.statementWrapper().allFilters();
        sortContract = source.statementWrapper().sortContract();
        restrictions.addAll(source.restrictions());
        if (source.filterDecorator() != null) {
            decorators.add(source.filterDecorator());
        }
    }

    public ConditionalStatementBuilder and(FilterData filter) {
        return and(expression(filter));
    }

    public ConditionalStatementBuilder and(AbstractStatement statement) {
        return append(statement, LogicOperator.CONJUNCTION);
    }

    public ConditionalStatementBuilder or(FilterData filter) {
        return or(expression(filter));
    }

    public ConditionalStatementBuilder or(AbstractStatement statement) {
        return append(statement, LogicOperator.DISJUNCTION);
    }

    public ConditionalStatementBuilder restrict(FilterData filter) {
        return restrict(expression(filter));
    }

    /** Adds a mandatory expression, validated immediately and intersected after all ordinary decorators. */
    public ConditionalStatementBuilder restrict(AbstractStatement statement) {
        Objects.requireNonNull(statement, "Restriction cannot be null");
        ConditionalStatement.validateRestriction(statement);
        restrictions.add(statement);
        return this;
    }

    /** Appends an ordinary decorator. Its runtime filter type must match the resolver and other decorators. */
    public ConditionalStatementBuilder decorate(FilterDecorator<?> decorator) {
        decorators.add(Objects.requireNonNull(decorator, "Decorator cannot be null"));
        return this;
    }

    /** Exposes data under every supplied path without generating an ordinary condition or sort alias. */
    public ConditionalStatementBuilder decoratedFilter(FilterData filter) {
        Objects.requireNonNull(filter, "Decorated filter cannot be null");
        var paths = new HashSet<String>();
        for (String path : filter.path()) {
            if (decoratedFilters.containsKey(path) || !paths.add(path)) {
                throw new IllegalArgumentException("Decorated filter path already exists: " + path);
            }
        }
        if (!ownsDecoratedFilters) {
            decoratedFilters = new LinkedHashMap<>(decoratedFilters);
            ownsDecoratedFilters = true;
        }
        for (String path : filter.path()) {
            decoratedFilters.put(path, filter);
        }
        return this;
    }

    public ConditionalStatement build() {
        Map<String, FilterData> data = ownsDecoratedFilters
                ? Collections.unmodifiableMap(new LinkedHashMap<>(decoratedFilters)) : decoratedFilters;
        return new ConditionalStatement(new StatementWrapper(ordinary, data, allFilters, sortContract), composeDecorators(), restrictions);
    }

    @SuppressWarnings("unchecked")
    private FilterDecorator<?> composeDecorators() {
        if (decorators.isEmpty()) {
            return null;
        }
        if (decorators.size() == 1) {
            return decorators.getFirst();
        }
        // The public wildcard contract cannot prove compatibility; consumers must supply matching runtime types.
        List<FilterDecorator<Object>> erasedDecorators = new ArrayList<>(decorators.size());
        for (FilterDecorator<?> decorator : decorators) {
            erasedDecorators.add((FilterDecorator<Object>) decorator);
        }
        return FilterDecorator.of(List.copyOf(erasedDecorators));
    }

    private ConditionalStatementBuilder append(AbstractStatement statement, LogicOperator operator) {
        Objects.requireNonNull(statement, "Statement cannot be null");
        ordinary = ordinary instanceof NoOpStatement ? statement : new CompoundStatement(ordinary, statement, operator);
        return this;
    }

    private static AbstractStatement expression(FilterData filter) {
        Objects.requireNonNull(filter, "Filter cannot be null");
        var statement = new LogicalStatement(filter);
        return filter.negate() ? new NegatedStatement(statement) : statement;
    }
}
