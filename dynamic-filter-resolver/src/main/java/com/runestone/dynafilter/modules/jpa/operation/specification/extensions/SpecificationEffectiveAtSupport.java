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

final class SpecificationEffectiveAtSupport {

    private SpecificationEffectiveAtSupport() {
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
        JpaExtensionPredicateUtils.requireValueCount(filterData, 1, operationName(boundMode));

        JpaPredicateUtils.PathResolution<?> startResolution = JpaPredicateUtils.resolveAttributePath(filterData.path()[0], filterData, root);
        JpaPredicateUtils.PathResolution<?> endResolution = JpaPredicateUtils.resolveAttributePath(filterData.path()[1], filterData, root);
        JpaPredicateUtils.applyDistinctIfNeeded(startResolution, query);
        JpaPredicateUtils.applyDistinctIfNeeded(endResolution, query);

        Object reference = JpaExtensionPredicateUtils.requireValue(filterData.findOneValue(), operationName(boundMode), "reference value");
        Expression<?> startExpression = startResolution.expression();
        Expression<?> endExpression = endResolution.expression();
        Object startReference = conversionService.convert(reference, startExpression.getJavaType());
        Object endReference = conversionService.convert(reference, endExpression.getJavaType());

        Predicate started = switch (boundMode) {
            case CLOSED, HALF_OPEN -> JpaPredicateUtils.toLessThanOrEqualToPredicate(criteriaBuilder, startExpression, startReference);
            case OPEN -> JpaPredicateUtils.toLessThanPredicate(criteriaBuilder, startExpression, startReference);
        };
        Predicate endMatches = switch (boundMode) {
            case CLOSED -> JpaPredicateUtils.toGreaterThanOrEqualToPredicate(criteriaBuilder, endExpression, endReference);
            case OPEN, HALF_OPEN -> JpaPredicateUtils.toGreaterThanPredicate(criteriaBuilder, endExpression, endReference);
        };
        return criteriaBuilder.and(started, criteriaBuilder.or(criteriaBuilder.isNull(endExpression), endMatches));
    }

    private static String operationName(IntervalBoundMode boundMode) {
        return switch (boundMode) {
            case CLOSED -> "EffectiveAtClosed";
            case OPEN -> "EffectiveAtOpen";
            case HALF_OPEN -> "EffectiveAtHalfOpen";
        };
    }

}
