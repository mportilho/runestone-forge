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

package com.runestone.dynafilter.modules.jpa.support;

import com.runestone.converters.DataConversionService;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

public final class JpaValues {

    private JpaValues() {
    }

    public static Object[] asArray(Object rawValues) {
        if (rawValues == null) {
            return new Object[]{null};
        }
        if (rawValues instanceof Object[] values) {
            return values;
        }
        if (rawValues instanceof Collection<?> values) {
            return values.toArray();
        }
        return new Object[]{rawValues};
    }

    public static Object[] convert(Object[] rawValues, Class<?> targetType, DataConversionService conversionService) {
        Objects.requireNonNull(rawValues, "rawValues cannot be null");
        Objects.requireNonNull(targetType, "targetType cannot be null");
        Objects.requireNonNull(conversionService, "conversionService cannot be null");
        return Arrays.stream(rawValues)
                .map(value -> value == null ? null : conversionService.convert(value, targetType))
                .toArray();
    }

}
