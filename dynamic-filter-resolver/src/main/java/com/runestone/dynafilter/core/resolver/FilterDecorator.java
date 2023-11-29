package com.runestone.dynafilter.core.resolver;

import com.runestone.dynafilter.core.generator.StatementWrapper;

import java.util.Collection;

/**
 * Decorates a filter with additional logic. Implementations of this interface must be <b>thread-safe</b> and <b>stateless</b>.
 *
 * @param <T> the type of the filter to be decorated
 */
public interface FilterDecorator<T> {

    /**
     * Decorates the given filter with additional logic.
     *
     * @param filter           the filter to be decorated
     * @param statementWrapper the statement container that was used to create the filter
     * @return the decorated filter
     */
    T decorate(T filter, StatementWrapper statementWrapper);

    /**
     * Creates a {@link FilterDecorator} that will apply all the given decorators in the order they are provided.
     *
     * @param decorators the decorators to be applied
     * @param <S>        the type of the filter to be decorated
     * @return a {@link FilterDecorator} that will apply all the given decorators in the order they are provided
     */
    static <S> FilterDecorator<S> of(Collection<FilterDecorator<S>> decorators) {
        return new CompositeFilterDecorator<>(decorators);
    }

}
