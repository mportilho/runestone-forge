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

import java.util.Objects;

public record FilterOperationMetadata(
        FilterValueShape valueShape,
        FilterArity pathArity,
        FilterArity valueArity,
        String description
) {

    public FilterOperationMetadata(FilterValueShape valueShape) {
        this(valueShape, FilterArity.any(), FilterArity.any(), null);
    }

    public FilterOperationMetadata {
        Objects.requireNonNull(valueShape, "valueShape cannot be null");
        Objects.requireNonNull(pathArity, "pathArity cannot be null");
        Objects.requireNonNull(valueArity, "valueArity cannot be null");
    }

    public static FilterOperationMetadata targetField() {
        return new FilterOperationMetadata(FilterValueShape.TARGET_FIELD);
    }

    public static FilterOperationMetadata stringValue() {
        return new FilterOperationMetadata(FilterValueShape.STRING);
    }

    public static FilterOperationMetadata booleanValue() {
        return new FilterOperationMetadata(FilterValueShape.BOOLEAN);
    }

    public static FilterOperationMetadata arrayValue() {
        return new FilterOperationMetadata(FilterValueShape.ARRAY);
    }

    public static FilterOperationMetadata dynamicValue() {
        return new FilterOperationMetadata(FilterValueShape.DYNAMIC, FilterArity.exactly(1), FilterArity.exactly(1), null);
    }
}
