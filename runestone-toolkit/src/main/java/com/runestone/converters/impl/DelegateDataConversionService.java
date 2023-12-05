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

package com.runestone.converters.impl;

import com.runestone.converters.DataConversionService;

public class DelegateDataConversionService implements DataConversionService {

    private final DefaultDataConversionService defaultDataConversionService;
    private final DataConversionService delegate;

    public DelegateDataConversionService(DataConversionService delegate) {
        this.defaultDataConversionService = new DefaultDataConversionService(false);
        this.delegate = delegate;
    }

    @Override
    public boolean canConvert(Class<?> sourceType, Class<?> targetType) {
        return defaultDataConversionService.canConvert(sourceType, targetType) || delegate.canConvert(sourceType, targetType);
    }

    @Override
    public <S, T> T convert(S source, Class<T> targetType) {
        T convertedValue = defaultDataConversionService.convert(source, targetType);
        if (convertedValue == null) {
            convertedValue = delegate.convert(source, targetType);
        }
        return convertedValue;
    }

}
