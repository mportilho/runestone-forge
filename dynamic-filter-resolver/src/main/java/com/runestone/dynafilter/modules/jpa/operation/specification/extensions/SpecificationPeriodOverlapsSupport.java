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

final class SpecificationPeriodOverlapsSupport {

    private SpecificationPeriodOverlapsSupport() {
    }

    static <T> Predicate toPredicate(
            FilterData filterData,
            DataConversionService conversionService,
            Root<T> root,
            CriteriaQuery<?> query,
            CriteriaBuilder criteriaBuilder,
            IntervalBoundMode boundMode
    ) {
        JpaExtensionPredicateUtils.requirePathCount(filterData, 2, operationName(boundMode));
        JpaExtensionPredicateUtils.requireValueCount(filterData, 2, operationName(boundMode));

        JpaPredicateUtils.PathResolution<?> startResolution = JpaPredicateUtils.resolveAttributePath(filterData.path()[0], filterData, root);
        JpaPredicateUtils.PathResolution<?> endResolution = JpaPredicateUtils.resolveAttributePath(filterData.path()[1], filterData, root);
        JpaPredicateUtils.applyDistinctIfNeeded(startResolution, query);
        JpaPredicateUtils.applyDistinctIfNeeded(endResolution, query);

        Object filterStart = JpaExtensionPredicateUtils.requireValue(filterData.findValueOnIndex(0), operationName(boundMode), "filter start");
        Object filterEnd = JpaExtensionPredicateUtils.requireValue(filterData.findValueOnIndex(1), operationName(boundMode), "filter end");
        Expression<?> startExpression = startResolution.expression();
        Expression<?> endExpression = endResolution.expression();
        Object convertedFilterEnd = conversionService.convert(filterEnd, startExpression.getJavaType());
        Object convertedFilterStart = conversionService.convert(filterStart, endExpression.getJavaType());

        Predicate startBeforeFilterEnd = switch (boundMode) {
            case CLOSED -> JpaPredicateUtils.toLessThanOrEqualToPredicate(criteriaBuilder, startExpression, convertedFilterEnd);
            case OPEN, HALF_OPEN -> JpaPredicateUtils.toLessThanPredicate(criteriaBuilder, startExpression, convertedFilterEnd);
        };
        Predicate endAfterFilterStart = switch (boundMode) {
            case CLOSED -> JpaPredicateUtils.toGreaterThanOrEqualToPredicate(criteriaBuilder, endExpression, convertedFilterStart);
            case OPEN, HALF_OPEN -> JpaPredicateUtils.toGreaterThanPredicate(criteriaBuilder, endExpression, convertedFilterStart);
        };
        return criteriaBuilder.and(startBeforeFilterEnd, criteriaBuilder.or(criteriaBuilder.isNull(endExpression), endAfterFilterStart));
    }

    private static String operationName(IntervalBoundMode boundMode) {
        return switch (boundMode) {
            case CLOSED -> "PeriodOverlapsClosed";
            case OPEN -> "PeriodOverlapsOpen";
            case HALF_OPEN -> "PeriodOverlapsHalfOpen";
        };
    }

}
