package com.runestone.dynafilter.core.resolver;

import com.runestone.dynafilter.core.exceptions.StatementGenerationException;
import com.runestone.dynafilter.core.generator.StatementWrapper;

import java.util.Collection;
import java.util.Objects;

public class CompositeFilterDecorator<T> implements FilterDecorator<T> {

    private final Collection<FilterDecorator<T>> decorators;

    public CompositeFilterDecorator(Collection<FilterDecorator<T>> decorators) {
        this.decorators = decorators;
    }

    @Override
    public T decorate(T filter, StatementWrapper statementWrapper) {
        Objects.requireNonNull(filter, "Filter cannot be null");
        Objects.requireNonNull(statementWrapper, "Statement wrapper cannot be null");
        T decorated = filter;
        for (FilterDecorator<T> decorator : decorators) {
            decorated = decorator.decorate(decorated, statementWrapper);
            if (decorated == null) {
                throw new StatementGenerationException("Filter decorator returned null: " + decorator.getClass().getName());
            }
        }
        return decorated;
    }

    public Collection<FilterDecorator<T>> getDecorators() {
        return decorators;
    }
}
