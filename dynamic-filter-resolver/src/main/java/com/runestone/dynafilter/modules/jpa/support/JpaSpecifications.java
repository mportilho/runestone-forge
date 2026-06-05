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
import com.runestone.dynafilter.core.model.FilterData;
import com.runestone.dynafilter.core.operation.support.FilterDataRequirements;
import jakarta.persistence.criteria.Expression;
import org.springframework.data.jpa.domain.Specification;

import java.util.Objects;

public final class JpaSpecifications {

    private JpaSpecifications() {
    }

    public static <T> Specification<T> effectiveAt(
            FilterData filterData,
            DataConversionService conversionService,
            IntervalBounds bounds
    ) {
        Objects.requireNonNull(filterData, "filterData cannot be null");
        Objects.requireNonNull(conversionService, "conversionService cannot be null");
        Objects.requireNonNull(bounds, "bounds cannot be null");

        return (root, query, criteriaBuilder) -> {
            String operationName = effectiveAtOperationName(bounds);
            FilterDataRequirements.requireShape(filterData, 2, 1, operationName);

            JpaPaths.ResolvedJpaPath<?> startResolution = JpaPaths.resolveAttributePath(filterData.path()[0], filterData, root);
            JpaPaths.ResolvedJpaPath<?> endResolution = JpaPaths.resolveAttributePath(filterData.path()[1], filterData, root);
            JpaPaths.applyDistinctIfNeeded(startResolution, query);
            JpaPaths.applyDistinctIfNeeded(endResolution, query);

            Object reference = FilterDataRequirements.requireValue(filterData.findOneValue(), operationName, "reference value");
            Expression<?> startExpression = startResolution.expression();
            Expression<?> endExpression = endResolution.expression();
            Object startReference = conversionService.convert(reference, startExpression.getJavaType());
            Object endReference = conversionService.convert(reference, endExpression.getJavaType());
            return JpaIntervalPredicates.effectiveAt(criteriaBuilder, startExpression, endExpression, startReference, endReference, bounds);
        };
    }

    public static <T> Specification<T> periodOverlaps(
            FilterData filterData,
            DataConversionService conversionService,
            IntervalBounds bounds
    ) {
        Objects.requireNonNull(filterData, "filterData cannot be null");
        Objects.requireNonNull(conversionService, "conversionService cannot be null");
        Objects.requireNonNull(bounds, "bounds cannot be null");

        return (root, query, criteriaBuilder) -> {
            String operationName = periodOverlapsOperationName(bounds);
            FilterDataRequirements.requireShape(filterData, 2, 2, operationName);

            JpaPaths.ResolvedJpaPath<?> startResolution = JpaPaths.resolveAttributePath(filterData.path()[0], filterData, root);
            JpaPaths.ResolvedJpaPath<?> endResolution = JpaPaths.resolveAttributePath(filterData.path()[1], filterData, root);
            JpaPaths.applyDistinctIfNeeded(startResolution, query);
            JpaPaths.applyDistinctIfNeeded(endResolution, query);

            Object filterStart = FilterDataRequirements.requireValue(filterData.findValueOnIndex(0), operationName, "filter start");
            Object filterEnd = FilterDataRequirements.requireValue(filterData.findValueOnIndex(1), operationName, "filter end");
            Expression<?> startExpression = startResolution.expression();
            Expression<?> endExpression = endResolution.expression();
            Object convertedFilterEnd = conversionService.convert(filterEnd, startExpression.getJavaType());
            Object convertedFilterStart = conversionService.convert(filterStart, endExpression.getJavaType());
            return JpaIntervalPredicates.periodOverlaps(
                    criteriaBuilder,
                    startExpression,
                    endExpression,
                    convertedFilterStart,
                    convertedFilterEnd,
                    bounds
            );
        };
    }

    public static <T> Specification<T> nullOrComparison(
            FilterData filterData,
            DataConversionService conversionService,
            ComparisonOperator operator
    ) {
        Objects.requireNonNull(filterData, "filterData cannot be null");
        Objects.requireNonNull(conversionService, "conversionService cannot be null");
        Objects.requireNonNull(operator, "operator cannot be null");

        return (root, query, criteriaBuilder) -> {
            String operationName = nullOrComparisonOperationName(operator);
            FilterDataRequirements.requireShape(filterData, 1, 1, operationName);

            JpaPaths.ResolvedJpaPath<?> resolution = JpaPaths.resolveAttributePath(filterData.path()[0], filterData, root);
            JpaPaths.applyDistinctIfNeeded(resolution, query);

            Expression<?> expression = resolution.expression();
            Object rawValue = FilterDataRequirements.requireValue(filterData.findOneValue(), operationName, "comparison value");
            Object convertedValue = conversionService.convert(rawValue, expression.getJavaType());
            return JpaComparisons.nullOr(criteriaBuilder, expression, operator, convertedValue);
        };
    }

    private static String effectiveAtOperationName(IntervalBounds bounds) {
        return switch (bounds) {
            case CLOSED -> "EffectiveAtClosed";
            case OPEN -> "EffectiveAtOpen";
            case START_INCLUSIVE_END_EXCLUSIVE -> "EffectiveAtHalfOpen";
        };
    }

    private static String periodOverlapsOperationName(IntervalBounds bounds) {
        return switch (bounds) {
            case CLOSED -> "PeriodOverlapsClosed";
            case OPEN -> "PeriodOverlapsOpen";
            case START_INCLUSIVE_END_EXCLUSIVE -> "PeriodOverlapsHalfOpen";
        };
    }

    private static String nullOrComparisonOperationName(ComparisonOperator operator) {
        return switch (operator) {
            case LESS_THAN -> "NullOrLess";
            case LESS_THAN_OR_EQUAL_TO -> "NullOrLessOrEquals";
            case GREATER_THAN -> "NullOrGreater";
            case GREATER_THAN_OR_EQUAL_TO -> "NullOrGreaterOrEquals";
        };
    }

}
