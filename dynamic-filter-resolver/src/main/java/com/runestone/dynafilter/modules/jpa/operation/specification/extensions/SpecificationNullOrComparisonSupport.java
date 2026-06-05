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
import com.runestone.dynafilter.core.model.FilterData;
import com.runestone.dynafilter.modules.jpa.operation.specification.JpaPredicateUtils;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

final class SpecificationNullOrComparisonSupport {

    private SpecificationNullOrComparisonSupport() {
    }

    static <T> Predicate toPredicate(
            FilterData filterData,
            DataConversionService conversionService,
            Root<T> root,
            CriteriaQuery<?> query,
            CriteriaBuilder criteriaBuilder,
            Mode mode
    ) {
        String operationName = operationName(mode);
        JpaExtensionPredicateUtils.requirePathCount(filterData, 1, operationName);
        JpaExtensionPredicateUtils.requireValueCount(filterData, 1, operationName);

        JpaPredicateUtils.PathResolution<?> resolution = JpaPredicateUtils.resolveAttributePath(filterData.path()[0], filterData, root);
        JpaPredicateUtils.applyDistinctIfNeeded(resolution, query);

        Expression<?> expression = resolution.expression();
        Object rawValue = JpaExtensionPredicateUtils.requireValue(filterData.findOneValue(), operationName, "comparison value");
        Object convertedValue = conversionService.convert(rawValue, expression.getJavaType());
        Predicate comparison = switch (mode) {
            case LESS -> JpaPredicateUtils.toLessThanPredicate(criteriaBuilder, expression, convertedValue);
            case LESS_OR_EQUALS -> JpaPredicateUtils.toLessThanOrEqualToPredicate(criteriaBuilder, expression, convertedValue);
            case GREATER -> JpaPredicateUtils.toGreaterThanPredicate(criteriaBuilder, expression, convertedValue);
            case GREATER_OR_EQUALS -> JpaPredicateUtils.toGreaterThanOrEqualToPredicate(criteriaBuilder, expression, convertedValue);
        };
        return criteriaBuilder.or(criteriaBuilder.isNull(expression), comparison);
    }

    private static String operationName(Mode mode) {
        return switch (mode) {
            case LESS -> "NullOrLess";
            case LESS_OR_EQUALS -> "NullOrLessOrEquals";
            case GREATER -> "NullOrGreater";
            case GREATER_OR_EQUALS -> "NullOrGreaterOrEquals";
        };
    }

    enum Mode {
        LESS,
        LESS_OR_EQUALS,
        GREATER,
        GREATER_OR_EQUALS
    }

}
