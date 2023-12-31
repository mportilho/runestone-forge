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

package com.runestone.dynafilter.modules.jpa.operation;

import com.runestone.converters.DataConversionService;
import com.runestone.dynafilter.core.operation.AbstractFilterOperationService;
import com.runestone.dynafilter.core.operation.DefinedFilterOperation;
import com.runestone.dynafilter.core.operation.FilterOperation;
import com.runestone.dynafilter.core.operation.types.*;
import com.runestone.dynafilter.modules.jpa.operation.specification.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.HashMap;
import java.util.Map;

public class SpecificationFilterOperationService extends AbstractFilterOperationService<Specification<?>> {

    public SpecificationFilterOperationService(DataConversionService conversionService) {
        super(() -> {
            Map<Class<? super DefinedFilterOperation>, FilterOperation<Specification<?>>> operations = new HashMap<>();
            operations.put(Between.class, (Between<Specification<?>>) filterData -> new SpecificationBetween<>(filterData, conversionService));
            operations.put(EndsWith.class, (EndsWith<Specification<?>>) filterData -> new SpecificationEndsWith<>(filterData, conversionService));
            operations.put(Equals.class, (Equals<Specification<?>>) filterData -> new SpecificationEquals<>(filterData, conversionService));
            operations.put(Greater.class, (Greater<Specification<?>>) filterData -> new SpecificationGreater<>(filterData, conversionService));
            operations.put(GreaterOrEquals.class, (GreaterOrEquals<Specification<?>>) filterData -> new SpecificationGreaterOrEquals<>(filterData, conversionService));
            operations.put(IsIn.class, (IsIn<Specification<?>>) filterData -> new SpecificationIsIn<>(filterData, conversionService));
            operations.put(IsNull.class, (IsNull<Specification<?>>) filterData -> new SpecificationIsNull<>(filterData, conversionService));
            operations.put(Less.class, (Less<Specification<?>>) filterData -> new SpecificationLess<>(filterData, conversionService));
            operations.put(LessOrEquals.class, (LessOrEquals<Specification<?>>) filterData -> new SpecificationLessOrEquals<>(filterData, conversionService));
            operations.put(Like.class, (Like<Specification<?>>) filterData -> new SpecificationLike<>(filterData, conversionService));
            operations.put(StartsWith.class, (StartsWith<Specification<?>>) filterData -> new SpecificationStartsWith<>(filterData, conversionService));
            return operations;
        });
    }

}
