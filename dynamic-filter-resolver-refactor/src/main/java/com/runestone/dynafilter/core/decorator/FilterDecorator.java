package com.runestone.dynafilter.core.decorator;

import com.runestone.dynafilter.core.generator.StatementWrapper;

import java.util.List;
import java.util.Objects;

public interface FilterDecorator<T> {

    T decorate(T filter, StatementWrapper statementWrapper);

    @SafeVarargs
    static <T> FilterDecorator<T> of(FilterDecorator<T>... decorators) {
        Objects.requireNonNull(decorators, "decorators must not be null");
        return new CompositeFilterDecorator<>(List.of(decorators));
    }
}
