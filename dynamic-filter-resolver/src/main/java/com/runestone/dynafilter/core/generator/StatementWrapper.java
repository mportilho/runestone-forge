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

package com.runestone.dynafilter.core.generator;

import com.runestone.dynafilter.core.model.FilterData;
import com.runestone.dynafilter.core.model.FilterRequestData;
import com.runestone.dynafilter.core.model.statement.AbstractStatement;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Wrapper class for a {@link AbstractStatement} and a {@link FilterData} map of decorated filters.
 *
 * @param statement        the statement for creating filters
 * @param decoratedFilters the map of decorated filters
 * @author Marcelo Portilho
 */
public record StatementWrapper(AbstractStatement statement, Map<String, FilterData> decoratedFilters,
                               List<FilterRequestData> allFilters) {

    public StatementWrapper(AbstractStatement statement, Map<String, FilterData> decoratedFilters, List<FilterRequestData> allFilters) {
        if (statement == null) {
            throw new IllegalArgumentException("Statement cannot be null");
        }
        this.statement = statement;
        this.decoratedFilters = decoratedFilters != null ? decoratedFilters : Map.of();
        this.allFilters = allFilters != null ? allFilters : List.of();
    }

    public Optional<FilterData> findDecoratedFilterByPath(String path) {
        return Optional.ofNullable(decoratedFilters.get(path));
    }
}
