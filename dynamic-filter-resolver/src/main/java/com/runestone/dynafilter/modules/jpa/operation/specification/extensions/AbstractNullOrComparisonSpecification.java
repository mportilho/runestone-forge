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
import com.runestone.dynafilter.modules.jpa.support.ComparisonOperator;
import com.runestone.dynafilter.modules.jpa.support.JpaComparisons;
import com.runestone.dynafilter.modules.jpa.support.JpaPaths;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.util.Objects;

abstract class AbstractNullOrComparisonSpecification<T> implements Specification<T> {

    private final FilterData filterData;
    private final DataConversionService dataConversionService;
    private final ComparisonOperator operator;

    AbstractNullOrComparisonSpecification(FilterData filterData, DataConversionService dataConversionService, ComparisonOperator operator) {
        this.filterData = Objects.requireNonNull(filterData, "filterData cannot be null");
        this.dataConversionService = Objects.requireNonNull(dataConversionService, "dataConversionService cannot be null");
        this.operator = Objects.requireNonNull(operator, "operator cannot be null");
    }

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        String operationName = operationName(operator);
        FilterDataRequirements.requireShape(filterData, 1, 1, operationName);

        JpaPaths.ResolvedJpaPath<?> resolution = JpaPaths.resolveAttributePath(filterData.path()[0], filterData, root, query);

        Expression<?> expression = resolution.expression();
        Object rawValue = FilterDataRequirements.requireValue(filterData.findOneValue(), operationName, "comparison value");
        Object convertedValue = dataConversionService.convert(rawValue, expression.getJavaType());
        return JpaComparisons.nullOr(criteriaBuilder, expression, operator, convertedValue);
    }

    private static String operationName(ComparisonOperator operator) {
        return switch (operator) {
            case LESS_THAN -> "NullOrLess";
            case LESS_THAN_OR_EQUAL_TO -> "NullOrLessOrEquals";
            case GREATER_THAN -> "NullOrGreater";
            case GREATER_THAN_OR_EQUAL_TO -> "NullOrGreaterOrEquals";
        };
    }

}
