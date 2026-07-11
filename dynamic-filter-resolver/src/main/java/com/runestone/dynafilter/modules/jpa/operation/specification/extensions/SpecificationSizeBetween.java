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

import com.runestone.converters.RuntimeDataConversionService;
import com.runestone.dynafilter.core.model.FilterData;
import com.runestone.dynafilter.core.operation.support.FilterDataRequirements;
import com.runestone.dynafilter.modules.jpa.support.JpaCollections;
import com.runestone.dynafilter.modules.jpa.support.JpaPaths;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collection;
import java.util.Objects;

public class SpecificationSizeBetween<T> implements Specification<T> {

    private static final String OPERATION_NAME = "SizeBetween";

    private final FilterData filterData;
    private final RuntimeDataConversionService dataConversionService;

    public SpecificationSizeBetween(FilterData filterData, RuntimeDataConversionService dataConversionService) {
        this.filterData = Objects.requireNonNull(filterData, "filterData cannot be null");
        this.dataConversionService = Objects.requireNonNull(dataConversionService, "dataConversionService cannot be null");
    }

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        FilterDataRequirements.requireShape(filterData, 1, 2, OPERATION_NAME);
        JpaPaths.ResolvedJpaPath<?> resolution = JpaPaths.resolveAttributePath(filterData.path()[0], filterData, root, query);
        Expression<Collection<?>> collectionExpression = JpaCollections.requireCollectionExpression(resolution.expression(), OPERATION_NAME);
        int lowerSize = JpaCollections.requireNonNegativeSize(filterData.findValueOnIndex(0), dataConversionService, OPERATION_NAME, "lower size");
        int upperSize = JpaCollections.requireNonNegativeSize(filterData.findValueOnIndex(1), dataConversionService, OPERATION_NAME, "upper size");
        if (lowerSize > upperSize) {
            throw new IllegalArgumentException("SizeBetween requires lower size to be less than or equal to upper size");
        }
        return criteriaBuilder.between(criteriaBuilder.size(collectionExpression), lowerSize, upperSize);
    }

}
