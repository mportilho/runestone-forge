package com.runestone.dynafilter.core.decorator;

import com.runestone.dynafilter.core.generator.StatementWrapper;

import java.util.List;
import java.util.Objects;

public final class CompositeFilterDecorator<T> implements FilterDecorator<T> {

    private final List<FilterDecorator<T>> decorators;

    public CompositeFilterDecorator(List<FilterDecorator<T>> decorators) {
        this.decorators = decorators == null ? List.of() : List.copyOf(decorators);
    }

    @Override
    public T decorate(T filter, StatementWrapper statementWrapper) {
        T current = Objects.requireNonNull(filter, "filter must not be null");
        Objects.requireNonNull(statementWrapper, "statementWrapper must not be null");
        for (FilterDecorator<T> decorator : decorators) {
            current = Objects.requireNonNull(decorator, "decorator must not be null")
                    .decorate(current, statementWrapper);
            if (current == null) {
                throw new IllegalStateException("FilterDecorator must not return null");
            }
        }
        return current;
    }
}
