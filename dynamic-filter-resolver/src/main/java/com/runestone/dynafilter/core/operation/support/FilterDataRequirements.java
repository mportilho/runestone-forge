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

package com.runestone.dynafilter.core.operation.support;

import com.runestone.dynafilter.core.exceptions.DynamicFilterConfigurationException;
import com.runestone.dynafilter.core.model.FilterData;

import java.util.Objects;

public final class FilterDataRequirements {

    private FilterDataRequirements() {
    }

    public static FilterData requireShape(FilterData filterData, int expectedPathCount, int expectedValueCount, String operationName) {
        requirePathCount(filterData, expectedPathCount, operationName);
        requireValueCount(filterData, expectedValueCount, operationName);
        return filterData;
    }

    public static void requirePathCount(FilterData filterData, int expectedCount, String operationName) {
        Objects.requireNonNull(filterData, "filterData cannot be null");
        if (filterData.path().length != expectedCount) {
            throw new DynamicFilterConfigurationException("%s requires exactly %d path(s)".formatted(operationName, expectedCount));
        }
    }

    public static void requireValueCount(FilterData filterData, int expectedCount, String operationName) {
        Objects.requireNonNull(filterData, "filterData cannot be null");
        if (filterData.values().length != expectedCount) {
            throw new DynamicFilterConfigurationException("%s requires exactly %d value(s)".formatted(operationName, expectedCount));
        }
    }

    public static Object requireValue(Object value, String operationName, String valueName) {
        if (value == null) {
            throw new IllegalArgumentException("%s requires non-null %s".formatted(operationName, valueName));
        }
        return value;
    }

}
