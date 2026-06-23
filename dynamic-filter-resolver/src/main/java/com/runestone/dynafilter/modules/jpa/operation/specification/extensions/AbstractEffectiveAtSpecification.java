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
import com.runestone.dynafilter.core.operation.support.FilterDataRequirements;
import com.runestone.dynafilter.modules.jpa.support.IntervalBounds;
import com.runestone.dynafilter.modules.jpa.support.JpaIntervalPredicates;
import com.runestone.dynafilter.modules.jpa.support.JpaPaths;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.util.Objects;

abstract class AbstractEffectiveAtSpecification<T> implements Specification<T> {

    private final FilterData filterData;
    private final DataConversionService dataConversionService;
    private final IntervalBounds bounds;

    AbstractEffectiveAtSpecification(FilterData filterData, DataConversionService dataConversionService, IntervalBounds bounds) {
        this.filterData = Objects.requireNonNull(filterData, "filterData cannot be null");
        this.dataConversionService = Objects.requireNonNull(dataConversionService, "dataConversionService cannot be null");
        this.bounds = Objects.requireNonNull(bounds, "bounds cannot be null");
    }

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        String operationName = operationName(bounds);
        FilterDataRequirements.requireShape(filterData, 2, 1, operationName);

        JpaPaths.ResolvedJpaPath<?> startResolution = JpaPaths.resolveAttributePath(filterData.path()[0], filterData, root, query);
        JpaPaths.ResolvedJpaPath<?> endResolution = JpaPaths.resolveAttributePath(filterData.path()[1], filterData, root, query);

        Object reference = FilterDataRequirements.requireValue(filterData.findOneValue(), operationName, "reference value");
        Expression<?> startExpression = startResolution.expression();
        Expression<?> endExpression = endResolution.expression();
        Object startReference = dataConversionService.convert(reference, startExpression.getJavaType());
        Object endReference = dataConversionService.convert(reference, endExpression.getJavaType());
        return JpaIntervalPredicates.effectiveAt(criteriaBuilder, startExpression, endExpression, startReference, endReference, bounds);
    }

    private static String operationName(IntervalBounds bounds) {
        return switch (bounds) {
            case CLOSED -> "EffectiveAtClosed";
            case OPEN -> "EffectiveAtOpen";
            case START_INCLUSIVE_END_EXCLUSIVE -> "EffectiveAtHalfOpen";
            case START_EXCLUSIVE_END_INCLUSIVE -> "EffectiveAtStartExclusiveEndInclusive";
        };
    }

}
