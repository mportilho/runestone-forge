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

import com.runestone.dynafilter.core.exceptions.DynamicFilterConfigurationException;
import com.runestone.dynafilter.core.generator.ConditionalStatement;
import com.runestone.dynafilter.core.generator.StatementWrapper;
import com.runestone.dynafilter.core.model.statement.AbstractStatement;

import java.util.List;

public interface DynamicFilterResolver<T> {

    <R extends T> R createFilter(StatementWrapper statementWrapper, FilterDecorator<T> decorator);

    @SuppressWarnings("unchecked")
    default <R extends T> R createFilter(ConditionalStatement conditionalStatement) {
        R filter = createFilter(conditionalStatement.statementWrapper(), (FilterDecorator<T>) conditionalStatement.filterDecorator());
        return conditionalStatement.restrictions().isEmpty() ? filter : applyRestrictions(filter, conditionalStatement.restrictions());
    }

    /**
     * Intersects every mandatory expression with the already decorated ordinary filter, without ordinary decorators.
     * Backends must reject absent executable restrictions and absent effective conditions. Implementations overriding
     * complete-statement resolution must honor this final phase or fail explicitly, never discard restrictions.
     * The wrapper-and-decorator entrypoint alone does not carry mandatory scope.
     */
    default <R extends T> R applyRestrictions(R filter, List<AbstractStatement> restrictions) {
        if (!restrictions.isEmpty()) {
            throw new DynamicFilterConfigurationException("This resolver does not support mandatory restrictions");
        }
        return filter;
    }

}
