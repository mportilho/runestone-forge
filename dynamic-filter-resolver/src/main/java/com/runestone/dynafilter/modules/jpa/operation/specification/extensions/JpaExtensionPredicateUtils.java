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

package com.runestone.dynafilter.modules.jpa.operation.specification.extensions;

import com.runestone.converters.DataConversionService;
import com.runestone.dynafilter.core.exceptions.DynamicFilterConfigurationException;
import com.runestone.dynafilter.core.model.FilterData;
import com.runestone.dynafilter.modules.jpa.operation.specification.JpaPredicateUtils;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.metamodel.Bindable;
import jakarta.persistence.metamodel.PluralAttribute;

import java.util.Collection;
import java.util.Objects;

final class JpaExtensionPredicateUtils {

    private JpaExtensionPredicateUtils() {
    }

    static void requirePathCount(FilterData filterData, int expectedCount, String operationName) {
        Objects.requireNonNull(filterData, "filterData cannot be null");
        if (filterData.path().length != expectedCount) {
            throw new DynamicFilterConfigurationException("%s requires exactly %d path(s)".formatted(operationName, expectedCount));
        }
    }

    static void requireValueCount(FilterData filterData, int expectedCount, String operationName) {
        Objects.requireNonNull(filterData, "filterData cannot be null");
        if (filterData.values().length != expectedCount) {
            throw new DynamicFilterConfigurationException("%s requires exactly %d value(s)".formatted(operationName, expectedCount));
        }
    }

    static Object requireValue(Object value, String operationName, String valueName) {
        if (value == null) {
            throw new IllegalArgumentException("%s requires non-null %s".formatted(operationName, valueName));
        }
        return value;
    }

    @SuppressWarnings("unchecked")
    static Expression<Collection<?>> collectionExpression(Expression<?> expression, String operationName) {
        Class<?> javaType = expression.getJavaType();
        if (!Collection.class.isAssignableFrom(javaType)) {
            throw new DynamicFilterConfigurationException("%s requires a collection path, but found %s"
                    .formatted(operationName, javaType.getCanonicalName()));
        }
        return (Expression<Collection<?>>) expression;
    }

    static Class<?> findCollectionElementType(Expression<?> expression) {
        if (expression instanceof Path<?> path) {
            Bindable<?> model = path.getModel();
            if (model instanceof PluralAttribute<?, ?, ?> pluralAttribute) {
                return pluralAttribute.getElementType().getJavaType();
            }
        }
        return Object.class;
    }

    static int convertSize(Object value, DataConversionService conversionService, String operationName, String valueName) {
        Object nonNullValue = requireValue(value, operationName, valueName);
        Integer converted = conversionService.convert(nonNullValue, Integer.class);
        if (converted == null || converted < 0) {
            throw new IllegalArgumentException("%s requires a non-negative integer %s".formatted(operationName, valueName));
        }
        return converted;
    }

}
