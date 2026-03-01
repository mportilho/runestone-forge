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

import com.runestone.converters.DataConverter;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

class DataConverterLoader {

    static Map<ConverterPairKey, DataConverter<?, ?>> loadConverters() {
        Map<ConverterPairKey, DataConverter<?, ?>> map = new HashMap<>();
        for (DataConverter<?, ?> converter : ServiceLoader.load(DataConverter.class)) {
            Class<?>[] typeArgs = resolveTypeArguments(converter.getClass());
            if (typeArgs != null) {
                Class<?> sourceClass = typeArgs[0];
                Class<?> targetClass = typeArgs[1];

                map.put(new ConverterPairKey(sourceClass, targetClass), converter);

                Class<?> primitiveTarget = getPrimitiveType(targetClass);
                if (primitiveTarget != null) {
                    map.put(new ConverterPairKey(sourceClass, primitiveTarget), converter);
                }
            }
        }
        return map;
    }

    private static Class<?>[] resolveTypeArguments(Class<?> clazz) {
        for (Type interfaceType : clazz.getGenericInterfaces()) {
            if (interfaceType instanceof ParameterizedType parameterizedType) {
                if (parameterizedType.getRawType() == DataConverter.class) {
                    Type[] typeArguments = parameterizedType.getActualTypeArguments();
                    if (typeArguments.length == 2) {
                        Class<?> sourceClass = eraseType(typeArguments[0]);
                        Class<?> targetClass = eraseType(typeArguments[1]);
                        if (sourceClass != null && targetClass != null) {
                            return new Class<?>[] { sourceClass, targetClass };
                        }
                    }
                }
            }
        }
        return null;
    }

    private static Class<?> eraseType(Type type) {
        if (type instanceof Class<?> clazz) {
            return clazz;
        } else if (type instanceof ParameterizedType parameterizedType) {
            return eraseType(parameterizedType.getRawType());
        }
        return null;
    }

    private static Class<?> getPrimitiveType(Class<?> wrapperClass) {
        if (wrapperClass == Boolean.class) return boolean.class;
        if (wrapperClass == Byte.class) return byte.class;
        if (wrapperClass == Character.class) return char.class;
        if (wrapperClass == Short.class) return short.class;
        if (wrapperClass == Integer.class) return int.class;
        if (wrapperClass == Long.class) return long.class;
        if (wrapperClass == Double.class) return double.class;
        if (wrapperClass == Float.class) return float.class;
        return null;
    }
}
