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

package com.runestone.dynafilter.modules.jpa.operation.specification;

import com.runestone.converters.DataConversionService;
import com.runestone.dynafilter.core.model.FilterData;
import com.runestone.dynafilter.core.model.modifiers.ModIgnoreCase;
import com.runestone.dynafilter.modules.jpa.support.JpaPaths;
import com.runestone.dynafilter.modules.jpa.support.JpaValues;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.Arrays;
import java.util.Collection;

public class SpecificationIsIn<T> implements Specification<T> {

    private final FilterData filterData;
    private final DataConversionService dataConversionService;

    public SpecificationIsIn(FilterData filterData, DataConversionService dataConversionService) {
        this.filterData = filterData;
        this.dataConversionService = dataConversionService;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        String path = filterData.path()[0];
        JpaPaths.ResolvedJpaPath<?> resolution = JpaPaths.resolveAttributePath(path, filterData, root);
        Expression expressionTemp = resolution.expression();
        Object[] arrayValues = JpaValues.asArray(filterData.values()[0]);

        boolean finalAttributeIsCollection = Collection.class.isAssignableFrom(expressionTemp.getJavaType());
        if (finalAttributeIsCollection) {
            resolution = JpaPaths.resolveAttributeJoinPath(path, filterData, root, query);
            expressionTemp = resolution.expression();
        } else {
            resolution = JpaPaths.resolveAttributePath(path, filterData, root, query);
            expressionTemp = resolution.expression();
        }

        boolean ignoreCase = expressionTemp.getJavaType().equals(String.class) && filterData.hasModifier(ModIgnoreCase.class);
        Expression expression = ignoreCase ? criteriaBuilder.upper(expressionTemp) : expressionTemp;
        final Class<?> targetType = expressionTemp.getJavaType();
        Object[] convertedValues = JpaValues.convert(arrayValues, targetType, dataConversionService);
        Object[] arr = ignoreCase
                ? Arrays.stream(convertedValues).map(value -> value != null ? value.toString().toUpperCase() : null).toArray()
                : convertedValues;
        return expression.in(arr);
    }

}
