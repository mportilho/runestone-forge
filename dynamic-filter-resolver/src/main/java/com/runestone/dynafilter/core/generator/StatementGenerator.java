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
     * <p>
     * This method <b>should not</b> return null. If no filters are to be generated, the method should return an instance of
     * StatementWrapper with {@link com.runestone.dynafilter.core.model.statement.NoOpStatement}
     * </p>
     *
     * @param filterInputs     inputs containing enough information to build the filter
     * @param filterParameters map of parameters providing values to be used to build the filter
     * @return a new instance of {@link AbstractStatement}
     */
    StatementWrapper generateStatements(T filterInputs, Map<String, Object> filterParameters);

}
