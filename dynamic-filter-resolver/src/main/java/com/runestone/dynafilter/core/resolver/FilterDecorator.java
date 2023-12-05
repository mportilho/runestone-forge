/*
 * MIT License
 * <p>
 * Copyright (c) 2023-2023 Marcelo Silva Portilho
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
