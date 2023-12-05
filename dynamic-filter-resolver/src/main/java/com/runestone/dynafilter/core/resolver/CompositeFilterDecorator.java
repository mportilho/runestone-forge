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
