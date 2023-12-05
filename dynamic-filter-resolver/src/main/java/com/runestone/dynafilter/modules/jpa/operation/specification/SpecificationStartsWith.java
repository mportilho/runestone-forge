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
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

public class SpecificationStartsWith<T> implements Specification<T> {

    private final FilterData filterData;
    private final DataConversionService dataConversionService;

    public SpecificationStartsWith(FilterData filterData, DataConversionService dataConversionService) {
        this.filterData = filterData;
        this.dataConversionService = dataConversionService;
    }

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        Path<String> path = JpaPredicateUtils.computeAttributePath(filterData, root);
        String value = dataConversionService.convert(filterData.findOneValue(), path.getJavaType());

        Expression<String> expression;
        if (filterData.hasModifier(ModIgnoreCase.class)) {
            expression = criteriaBuilder.upper(path);
            value = value != null ? value.toUpperCase() + "%" : null;
        } else {
            expression = path;
            value = value != null ? value + "%" : null;
        }
        return criteriaBuilder.like(expression, value);
    }

}
