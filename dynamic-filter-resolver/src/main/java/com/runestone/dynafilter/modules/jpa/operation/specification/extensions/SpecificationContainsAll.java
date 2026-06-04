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
import org.springframework.data.jpa.domain.Specification;

import java.util.Collection;
import java.util.Objects;

public class SpecificationContainsAll<T> implements Specification<T> {

    private static final String OPERATION_NAME = "ContainsAll";

    private final FilterData filterData;
    private final DataConversionService dataConversionService;

    public SpecificationContainsAll(FilterData filterData, DataConversionService dataConversionService) {
        this.filterData = Objects.requireNonNull(filterData, "filterData cannot be null");
        this.dataConversionService = Objects.requireNonNull(dataConversionService, "dataConversionService cannot be null");
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        JpaExtensionPredicateUtils.requirePathCount(filterData, 1, OPERATION_NAME);
        JpaExtensionPredicateUtils.requireValueCount(filterData, 1, OPERATION_NAME);
        JpaPredicateUtils.PathResolution<?> resolution = JpaPredicateUtils.resolveAttributePath(filterData.path()[0], filterData, root);
        Expression<Collection<?>> collectionExpression = JpaExtensionPredicateUtils.collectionExpression(resolution.expression(), OPERATION_NAME);
        Object[] rawValues = JpaPredicateUtils.valuesAsArray(filterData.findOneValue());
        if (rawValues.length == 0) {
            return criteriaBuilder.conjunction();
        }

        Class<?> elementType = JpaExtensionPredicateUtils.findCollectionElementType(collectionExpression);
        Object[] values = JpaPredicateUtils.convertValues(rawValues, elementType, dataConversionService);
        Predicate[] predicates = new Predicate[values.length];
        for (int i = 0; i < values.length; i++) {
            predicates[i] = criteriaBuilder.isMember(values[i], (Expression) collectionExpression);
        }
        return criteriaBuilder.and(predicates);
    }

}
