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

package com.runestone.dynafilter.core.operation;

import com.runestone.dynafilter.core.exceptions.DynamicFilterConfigurationException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class FilterOperationRegistry<T> {

    @SuppressWarnings("rawtypes")
    private final Map<Class<? extends FilterOperation>, FilterOperation<T>> operations = new LinkedHashMap<>();

    @SuppressWarnings("rawtypes")
    public void register(Class<? extends FilterOperation> operationType, FilterOperation<T> operation) {
        Objects.requireNonNull(operationType, "operationType cannot be null");
        Objects.requireNonNull(operation, "operation cannot be null");

        FilterOperation<T> previous = operations.putIfAbsent(operationType, operation);
        if (previous != null) {
            throw new DynamicFilterConfigurationException(
                    "Filter operation '%s' is already registered".formatted(operationType.getCanonicalName())
            );
        }
    }

    @SuppressWarnings("rawtypes")
    Map<Class<? extends FilterOperation>, FilterOperation<T>> toMap() {
        return Map.copyOf(operations);
    }

}
