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

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.RandomAccess;

/**
 * Encapsulates the optimized {@code Collection/List -> array} conversion paths used by
 * {@link DefaultDataConversionService}. This class is responsible only for building target arrays
 * efficiently, including specialized primitive loops and direct reference-array writes, while the
 * owning conversion service remains responsible for conversion policy and null/failure semantics.
 */
final class CollectionArrayConversionSupport {

    private CollectionArrayConversionSupport() {
    }

    static boolean canConvert(Class<?> sourceType, Class<?> targetType) {
        return targetType.isArray() && Collection.class.isAssignableFrom(sourceType);
    }

    @SuppressWarnings("unchecked")
    static <T> T convert(Object source, Class<T> targetType, DataConversionService conversionService) {
        if (!targetType.isArray() || !(source instanceof Collection<?> collection)) {
            return null;
        }

        Class<?> componentType = targetType.getComponentType();
        if (source instanceof List<?> list && source instanceof RandomAccess) {
            return (T) convertRandomAccessListToArray(list, componentType, conversionService);
        }
        return (T) convertCollectionElementsToArray(collection, componentType, conversionService);
    }

    private static Object convertRandomAccessListToArray(List<?> source, Class<?> componentType, DataConversionService conversionService) {
        int size = source.size();
        if (componentType == int.class) {
            int[] array = new int[size];
            for (int i = 0; i < size; i++) {
                Integer converted = convertElement(source.get(i), Integer.class, conversionService);
                if (converted == null) {
                    return null;
                }
                array[i] = converted;
            }
            return array;
        }
        if (componentType == long.class) {
            long[] array = new long[size];
            for (int i = 0; i < size; i++) {
                Long converted = convertElement(source.get(i), Long.class, conversionService);
                if (converted == null) {
                    return null;
                }
                array[i] = converted;
            }
            return array;
        }
        if (componentType == double.class) {
            double[] array = new double[size];
            for (int i = 0; i < size; i++) {
                Double converted = convertElement(source.get(i), Double.class, conversionService);
                if (converted == null) {
                    return null;
                }
                array[i] = converted;
            }
            return array;
        }
        if (componentType == float.class) {
            float[] array = new float[size];
            for (int i = 0; i < size; i++) {
                Float converted = convertElement(source.get(i), Float.class, conversionService);
                if (converted == null) {
                    return null;
                }
                array[i] = converted;
            }
            return array;
        }
        if (componentType == short.class) {
            short[] array = new short[size];
            for (int i = 0; i < size; i++) {
                Short converted = convertElement(source.get(i), Short.class, conversionService);
                if (converted == null) {
                    return null;
                }
                array[i] = converted;
            }
            return array;
        }
        if (componentType == byte.class) {
            byte[] array = new byte[size];
            for (int i = 0; i < size; i++) {
                Byte converted = convertElement(source.get(i), Byte.class, conversionService);
                if (converted == null) {
                    return null;
                }
                array[i] = converted;
            }
            return array;
        }
        if (componentType == boolean.class) {
            boolean[] array = new boolean[size];
            for (int i = 0; i < size; i++) {
                Boolean converted = convertElement(source.get(i), Boolean.class, conversionService);
                if (converted == null) {
                    return null;
                }
                array[i] = converted;
            }
            return array;
        }
        if (componentType == char.class) {
            char[] array = new char[size];
            for (int i = 0; i < size; i++) {
                Character converted = convertElement(source.get(i), Character.class, conversionService);
                if (converted == null) {
                    return null;
                }
                array[i] = converted;
            }
            return array;
        }

        Object[] array = (Object[]) Array.newInstance(componentType, size);
        for (int i = 0; i < size; i++) {
            Object element = source.get(i);
            Object converted = conversionService.convert(element, componentType);
            if (element != null && converted == null) {
                return null;
            }
            array[i] = converted;
        }
        return array;
    }

    private static Object convertCollectionElementsToArray(Collection<?> source, Class<?> componentType,
                                                           DataConversionService conversionService) {
        int size = source.size();
        Iterator<?> iterator = source.iterator();

        if (componentType == int.class) {
            int[] array = new int[size];
            for (int i = 0; i < size; i++) {
                Integer converted = convertElement(iterator.next(), Integer.class, conversionService);
                if (converted == null) {
                    return null;
                }
                array[i] = converted;
            }
            return array;
        }
        if (componentType == long.class) {
            long[] array = new long[size];
            for (int i = 0; i < size; i++) {
                Long converted = convertElement(iterator.next(), Long.class, conversionService);
                if (converted == null) {
                    return null;
                }
                array[i] = converted;
            }
            return array;
        }
        if (componentType == double.class) {
            double[] array = new double[size];
            for (int i = 0; i < size; i++) {
                Double converted = convertElement(iterator.next(), Double.class, conversionService);
                if (converted == null) {
                    return null;
                }
                array[i] = converted;
            }
            return array;
        }
        if (componentType == float.class) {
            float[] array = new float[size];
            for (int i = 0; i < size; i++) {
                Float converted = convertElement(iterator.next(), Float.class, conversionService);
                if (converted == null) {
                    return null;
                }
                array[i] = converted;
            }
            return array;
        }
        if (componentType == short.class) {
            short[] array = new short[size];
            for (int i = 0; i < size; i++) {
                Short converted = convertElement(iterator.next(), Short.class, conversionService);
                if (converted == null) {
                    return null;
                }
                array[i] = converted;
            }
            return array;
        }
        if (componentType == byte.class) {
            byte[] array = new byte[size];
            for (int i = 0; i < size; i++) {
                Byte converted = convertElement(iterator.next(), Byte.class, conversionService);
                if (converted == null) {
                    return null;
                }
                array[i] = converted;
            }
            return array;
        }
        if (componentType == boolean.class) {
            boolean[] array = new boolean[size];
            for (int i = 0; i < size; i++) {
                Boolean converted = convertElement(iterator.next(), Boolean.class, conversionService);
                if (converted == null) {
                    return null;
                }
                array[i] = converted;
            }
            return array;
        }
        if (componentType == char.class) {
            char[] array = new char[size];
            for (int i = 0; i < size; i++) {
                Character converted = convertElement(iterator.next(), Character.class, conversionService);
                if (converted == null) {
                    return null;
                }
                array[i] = converted;
            }
            return array;
        }

        Object[] array = (Object[]) Array.newInstance(componentType, size);
        for (int i = 0; i < size; i++) {
            Object element = iterator.next();
            Object converted = conversionService.convert(element, componentType);
            if (element != null && converted == null) {
                return null;
            }
            array[i] = converted;
        }
        return array;
    }

    private static <T> T convertElement(Object source, Class<T> targetType, DataConversionService conversionService) {
        T converted = conversionService.convert(source, targetType);
        if (source != null && converted == null) {
            return null;
        }
        return converted;
    }
}
